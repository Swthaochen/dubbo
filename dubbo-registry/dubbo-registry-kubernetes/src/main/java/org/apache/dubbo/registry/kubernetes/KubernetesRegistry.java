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

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.url.component.DubboServiceAddressURL;
import org.apache.dubbo.common.url.component.ServiceConfigURL;
import org.apache.dubbo.common.url.component.URLAddress;
import org.apache.dubbo.common.url.component.URLParam;
import org.apache.dubbo.registry.NotifyListener;
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

    }

    @Override
    public void doUnregister(URL url) {

    }

    @Override
    public void doSubscribe(URL url, NotifyListener listener) {
        List<URL> urls = new LinkedList<>();

        String address = "dubbo://" + "192.168.115.137" + ":" + "31312" + "/org.apache.dubbo.demo.api.DemoService";
        String params = "anyhost%3Dtrue%26application%3Dapi-dubbo-provider%26deprecated%3Dfalse%26dubbo%3D2.0.2%26dynamic%3Dtrue%26generic%3Dfalse%26interface%3Dorg.apache.dubbo.demo.api.DemoService%26methods%3DsayHello%26release%3D1.0-SNAPSHOT%26revision%3D1.0-SNAPSHOT%26service-name-mapping%3Dtrue%26side%3Dprovider";

        List<String> categories = toCategories(url);

        if (categories.contains(PROVIDERS_CATEGORY)){
            ServiceConfigURL consumerURL = new ServiceConfigURL("dubbo", null, null, "10.100.3.213", 0, "org.apache.dubbo.demo.api.DemoService");
            Map<String, String> paramsPair = StringToMap("application=api-dubbo-consumer&category=providers,configurators,routers&interface=org.apache.dubbo.demo.api.DemoService&pid=5524&side=consumer&sticky=false");
            URL consumer = consumerURL.addParameters(paramsPair);

            URLAddress urlAddress = URLAddress.parse(address, "dubbo", false);
            URLParam urlParam = URLParam.parse(params, true, null);
            DubboServiceAddressURL providerURL = new DubboServiceAddressURL(urlAddress, urlParam, consumer, null);
            urls.add(providerURL);
        }

        if (categories.contains(CONFIGURATORS_CATEGORY)) {
            ServiceConfigURL configureURL = new ServiceConfigURL("empty", null, null, "10.100.3.213", 0, "org.apache.dubbo.demo.api.DemoService");
            Map<String, String> configureParamsPair = StringToMap("side=consumer&interface=org.apache.dubbo.demo.api.DemoService&pid=11800&application=api-dubbo-consumer&dubbo=2.0.2&release=3.0.3-SNAPSHOT&sticky=false&category=configurators&methods=sayHello&timestamp=1632189983925");
            URL configureURLUrl = configureURL.addParameters(configureParamsPair);
            urls.add(configureURLUrl);
        }

        if (categories.contains(ROUTERS_CATEGORY)) {
            ServiceConfigURL routerURL = new ServiceConfigURL("empty", null, null, "10.100.3.213", 0, "org.apache.dubbo.demo.api.DemoService");
            Map<String, String> routerParamsPair = StringToMap("side=consumer&interface=org.apache.dubbo.demo.api.DemoService&pid=11800&application=api-dubbo-consumer&dubbo=2.0.2&release=3.0.3-SNAPSHOT&sticky=false&category=routers&methods=sayHello&timestamp=1632189983925");
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
