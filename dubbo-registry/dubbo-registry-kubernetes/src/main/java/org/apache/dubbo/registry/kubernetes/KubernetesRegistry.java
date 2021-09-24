/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.registry.kubernetes;

import com.alibaba.fastjson.JSONObject;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.url.component.DubboServiceAddressURL;
import org.apache.dubbo.common.url.component.ServiceConfigURL;
import org.apache.dubbo.common.url.component.URLAddress;
import org.apache.dubbo.common.url.component.URLParam;
import org.apache.dubbo.registry.NotifyListener;
import org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst;
import org.apache.dubbo.registry.kubernetes.util.KubernetesConfigUtils;
import org.apache.dubbo.registry.support.FailbackRegistry;

import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.apache.dubbo.common.constants.CommonConstants.ANY_VALUE;
import static org.apache.dubbo.common.constants.CommonConstants.PATH_SEPARATOR;
import static org.apache.dubbo.common.constants.RegistryConstants.*;

/**
 * Empty implements for Kubernetes <br/>
 * Kubernetes only support `Service Discovery` mode register <br/>
 * Used to compat past version like 2.6.x, 2.7.x with interface level register <br/>
 * {@link KubernetesServiceDiscovery} is the real implementation of Kubernetes
 */
public class KubernetesRegistry extends FailbackRegistry {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public final static String KUBERNETES_PROVIDERS_KEY = "io.dubbo/providers";
    public final static String KUBERNETES_PATHNAME_KEY = "io.dubbo/pathname";

    public KubernetesRegistry(URL url) {
        super(url);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    // Sending a registration request to the server side
    @Override
    public void doRegister(URL url) {
        Map<String, String> parameters = url.getParameters();
        if (parameters.get("side").equals("consumer")) return;
        URL registryUrl = this.getUrl();
        String registryAddress = registryUrl.getAddress();
        Config config = KubernetesConfigUtils.testK8sInitConfig(registryAddress);
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        String currentHostname = System.getenv("HOSTNAME");
        String namespace = config.getNamespace();

        // Get Ip and Port for Comsumers to request
        String protocol = url.getProtocol();
        String address = registryUrl.getIp();
        int nodeport = Integer.parseInt(url.getParameter("nodeport"));
        String path = url.getPath();

        ServiceConfigURL configURL = new ServiceConfigURL(protocol, null, null, address, nodeport, path);
        URL providerUrl = configURL.addParameters(parameters);

        boolean availableAccess;
        try {
            availableAccess = kubernetesClient.pods().inNamespace(namespace).withName(currentHostname).get() != null;
        } catch (Throwable e) {
            availableAccess = false;
        }
        if (!availableAccess) {
            String message = "Unable to access api server. " +
                "Please check your url config." +
                " Master URL: " + config.getMasterUrl() +
                " Hostname: " + currentHostname;
            logger.error(message);
        } else {
            logger.info("Successfully to init pod in hostname : " + currentHostname);
            KubernetesMeshEnvListener.injectKubernetesEnv(kubernetesClient, namespace);
        }

        kubernetesClient.pods()
            .inNamespace(namespace)
            .withName(currentHostname)
            .edit(pod ->
                new PodBuilder(pod)
                    .editOrNewMetadata()
                    .addToLabels(KUBERNETES_PATHNAME_KEY, path)
                    .addToAnnotations(KUBERNETES_PROVIDERS_KEY, providerUrl.toFullString())
                    .endMetadata()
                    .build());
        kubernetesClient.close();
    }

    @Override
    public void doUnregister(URL url) {

    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        Map<String, String> parameters = url.getParameters();
        if (parameters.get("side").equals("provider")) return;
        URL registryUrl = this.getUrl();
        String registryAddress = registryUrl.getAddress();
        Config config = KubernetesConfigUtils.testK8sInitConfig(registryAddress);
        KubernetesClient kubernetesClient = new DefaultKubernetesClient(config);
        String namespace = config.getNamespace();
        String path = url.getPath();

        Pod pod = kubernetesClient.pods().inNamespace(namespace).withLabel(KUBERNETES_PATHNAME_KEY, path).list().getItems().get(0);
        ObjectMeta metadata = pod.getMetadata();
        Map<String, String> annotations = metadata.getAnnotations();
        String s = annotations.get(KUBERNETES_PROVIDERS_KEY);
        String[] parts = new String[2];
        int index = s.indexOf('?');
        if (index == -1) {
            return;
        } else {
            parts[0] = s.substring(0, index);
            parts[1] = s.substring(index+1);
        }
        List<URL> urls = new LinkedList<>();
        String address = parts[0];
        String params = parts[1];

        List<String> categories = toCategories(url);

        if (categories.contains(PROVIDERS_CATEGORY)){
            URLAddress urlAddress = URLAddress.parse(address, "dubbo", false);
            URLParam urlParam = URLParam.parse(params, true, null);
            DubboServiceAddressURL providerURL = new DubboServiceAddressURL(urlAddress, urlParam, url, null);
            urls.add(providerURL);
        }

        if (categories.contains(CONFIGURATORS_CATEGORY)) {
            ServiceConfigURL configureURL = new ServiceConfigURL("empty", null, null, url.getIp(), 0, url.getPath());
            Map<String, String> configureParamsPair = url.getParameters();
            configureParamsPair.put("category", "configurators");
            URL configureURLUrl = configureURL.addParameters(configureParamsPair);
            urls.add(configureURLUrl);
        }

        if (categories.contains(ROUTERS_CATEGORY)) {
            ServiceConfigURL routerURL = new ServiceConfigURL("empty", null, null, url.getIp(), 0, url.getPath());
            Map<String, String> routerParamsPair = url.getParameters();
            routerParamsPair.put("category", "routers");
            URL routerURLUrl = routerURL.addParameters(routerParamsPair);
            urls.add(routerURLUrl);
        }

        if (categories.contains(CONSUMERS_CATEGORY)) {
            return;
        }
        notify(url, listener, urls);

    }

    @Override
    public void doUnsubscribe(URL url, NotifyListener listener) {

    }
    public static Map<String, String> StringToMap(String str){
        String[] split = str.split("&");
        Map<String, String> params = new HashMap<>();
        for (String s : split) {
            String[] kv = s.split("=");
            params.put(kv[0], kv[1]);
        }
        return params;
    }

    private List<String> toCategories(URL url) {
        String[] categories;
        if (ANY_VALUE.equals(url.getCategory())) {
            categories = new String[]{PROVIDERS_CATEGORY, CONSUMERS_CATEGORY, ROUTERS_CATEGORY, CONFIGURATORS_CATEGORY};
        } else {
            categories = url.getCategory(new String[]{DEFAULT_CATEGORY});
        }
        return new ArrayList<>(Arrays.asList(categories));
    }
}
