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
package org.apache.dubbo.registry.kubernetes.util;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.StringUtils;

import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Base64;
import java.util.Properties;

import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.API_VERSION;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.NAMESPACE;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.USERNAME;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.PASSWORD;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.OAUTH_TOKEN;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CA_CERT_FILE;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CA_CERT_DATA;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CLIENT_KEY_FILE;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CLIENT_KEY_DATA;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CLIENT_CERT_FILE;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CLIENT_CERT_DATA;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CLIENT_KEY_ALGO;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CLIENT_KEY_PASSPHRASE;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.CONNECTION_TIMEOUT;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.REQUEST_TIMEOUT;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.WATCH_RECONNECT_INTERVAL;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.ROLLING_TIMEOUT;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.WATCH_RECONNECT_LIMIT;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.LOGGING_INTERVAL;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.TRUST_CERTS;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.HTTP2_DISABLE;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.HTTP_PROXY;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.HTTPS_PROXY;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.PROXY_USERNAME;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.PROXY_PASSWORD;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.NO_PROXY;
import static org.apache.dubbo.registry.kubernetes.util.KubernetesClientConst.USE_HTTPS;

public class KubernetesConfigUtils {

    public final static String K8SUSERKEYS_PATH = "./config.properties";

    public static Config createKubernetesConfig(URL url) {
        // Init default config
        Config base = Config.autoConfigure(null);

        // replace config with parameters if presents
        return new ConfigBuilder(base) //
            .withMasterUrl(buildMasterUrl(url)) //
            .withApiVersion(url.getParameter(API_VERSION, base.getApiVersion())) //
//            .withNamespace(url.getParameter(NAMESPACE, base.getNamespace())) //
//            .withUsername(url.getParameter(USERNAME, base.getUsername())) //
//            .withPassword(url.getParameter(PASSWORD, base.getPassword())) //

//            .withOauthToken(url.getParameter(OAUTH_TOKEN, base.getOauthToken())) //
//
//            .withCaCertFile(url.getParameter(CA_CERT_FILE,base.getCaCertFile())) //
            .withCaCertData(url.getParameter(CA_CERT_DATA,decodeBase64(base.getCaCertData()))) //

//            .withClientKeyFile(url.getParameter(CLIENT_KEY_FILE,base.getClientKeyFile())) //
            .withClientKeyData(url.getParameter(CLIENT_KEY_DATA, decodeBase64(base.getClientKeyData()))) //

//            .withClientCertFile(url.getParameter(CLIENT_CERT_FILE,base.getClientCertFile())) //
            .withClientCertData(url.getParameter(CLIENT_CERT_DATA,decodeBase64(base.getClientCertData()))) //

//            .withClientKeyAlgo(url.getParameter(CLIENT_KEY_ALGO,base.getClientKeyAlgo())) //
            .withClientKeyPassphrase(url.getParameter(CLIENT_KEY_PASSPHRASE,base.getClientKeyPassphrase())) //

            .withConnectionTimeout(url.getParameter(CONNECTION_TIMEOUT,base.getConnectionTimeout())) //
            .withRequestTimeout(url.getParameter(REQUEST_TIMEOUT,base.getRequestTimeout())) //
            .withRollingTimeout(url.getParameter(ROLLING_TIMEOUT,base.getRollingTimeout())) //

            .withWatchReconnectInterval(url.getParameter(WATCH_RECONNECT_INTERVAL,base.getWatchReconnectInterval())) //
            .withWatchReconnectLimit(url.getParameter(WATCH_RECONNECT_LIMIT,base.getWatchReconnectLimit())) //
            .withLoggingInterval(url.getParameter(LOGGING_INTERVAL,base.getLoggingInterval())) //

            .withTrustCerts(url.getParameter(TRUST_CERTS,base.isTrustCerts())) //
            .withHttp2Disable(url.getParameter(HTTP2_DISABLE,base.isTrustCerts())) //

//            .withHttpProxy(url.getParameter(HTTP_PROXY,base.getHttpProxy())) //
//            .withHttpsProxy(url.getParameter(HTTPS_PROXY,base.getHttpsProxy())) //
//            .withProxyUsername(url.getParameter(PROXY_USERNAME,base.getProxyUsername())) //
//            .withProxyPassword(url.getParameter(PROXY_PASSWORD,base.getProxyPassword())) //
//            .withNoProxy(url.getParameter(NO_PROXY,base.getNoProxy())) //
            .build();
    }

    public static Config testK8sInitConfig(String address) {

        KubernetesConfigUtils kubernetesConfigUtils = new KubernetesConfigUtils();
        K8sUser k8sUser = kubernetesConfigUtils.new K8sUser();
        KubernetesConfigUtils.initAuthentification(k8sUser);

//        String caCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUM1ekNDQWMrZ0F3SUJBZ0lCQURBTkJna3Foa2lHOXcwQkFRc0ZBREFWTVJNd0VRWURWUVFERXdwcmRXSmwKY201bGRHVnpNQjRYRFRJeE1EZ3pNVEF6TXpJeE4xb1hEVE14TURneU9UQXpNekl4TjFvd0ZURVRNQkVHQTFVRQpBeE1LYTNWaVpYSnVaWFJsY3pDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBTXdSCjF0V29GQ1k4MnlPWlUvNE1yU3l1WFhOa1NjTkVFVWtKYmJTVElQRlVKeVI0VERDWVB6L1dmOUdCVDNIbTdZU2QKMG8wclk0SUVRMnVBUFYvRTFmTjRzSGNpeGR1SlV6ZEtUaEdudDJWWFp2ekptODhkK2VLWndaTFJ6aU1kSVlkawo1aFQ4bTN4ZnJCOFlqOEk3c2tucWltWHJGbXMxTFdRZFYxc3NxVTVWUEJhTWQ2U2FwcUZjTjluRWJhTmdJNm5KCjAzdmNKanZ6NDRtVGt4Sit3QmFab2pKU2Y1MEFnKzFXNnFHbVVhUTdjMXJpc1J2V3NlUXdnRmtkM1dXc2ZCWVAKWXdkb3FlNUR0SC9WUmo2SzY4YnZZcDJaK3gyQ0FTUVgvcUZ5dUNiMTlMZkVKOXdENzVmemc3M29CTm43RDcwZwo4amErcFN0dEtMUWdtMmZPZGNVQ0F3RUFBYU5DTUVBd0RnWURWUjBQQVFIL0JBUURBZ0trTUE4R0ExVWRFd0VCCi93UUZNQU1CQWY4d0hRWURWUjBPQkJZRUZMSWpqdE1lUmlPUEhjM0QwQkpPeit4aDdLck9NQTBHQ1NxR1NJYjMKRFFFQkN3VUFBNElCQVFDRVpGeFR3Q3MveitKb01MOVlDT0ZMSExNSTdSemUwMjVtS3NYak9PeGk1azN1elRRYwp2cmVxTXZUdk5EWGZtNWVWczMvd2k1ZTZZTmQ2aTBTTFVoOGVaaHVoM3BsdGFzcFdQaFczdGVEWkRQV2VQWTIrClp5QnBZTThmb28reC9WKzV3U0JtbzhxUWlaM0RYeUlCbStldjkxSUlRQW5FTDhaazhvT2V5UXZxSjFtR2E1eTcKSFMrTm5OdzJvMkZaQSt6NnorYlBpVTFYenA4b0hWNVV2cWdpQjV5VlBnNEpTZk1MK2NHNEwzNGJMZmtZak14UgpBdFNQZndzWVdhcjc4Q3lWL1JUQ1cwbkhJNzFCeHRFekY0SzNXZEVJaGpKNTh1aCtXbDRjTWNTa2I3ZUZOakUyCnFpU2tlK1hRR2NLTXBHbG54Q2hwUnlXK1g4SXZSM25rbFk4dAotLS0tLUVORCBDRVJUSUZJQ0FURS0tLS0tCg==";
//        String clientKeyData = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFb3dJQkFBS0NBUUVBb1VWTmpOL2ZzOUpOWU9rTGdreFdGWjlzaTRGZ2VZM29NYXU3WkV4RGw4enIzcUVGCkRwazUzbi91aFAyR3hYdW9Dajh6NlRzTkluTmFBdHRjaXZCd2lFUE5JZ28yVUliNjZyK24yNiszZHdsYm84Y3AKeG93YXFVQXZ1LzhtMmZRNlFzaDhsY0xHODNZWlV4Q1FLdW44Z3daQ2s5OUx2QURRaXFxZkdQdk11SVlUS2R1eQpQMFRoOXJmZjdjeHpLRHpFS3lPaExYc1d5RlRCSDM1YmY3R21FUGxrbmhqVWZDUUUyRnBVZUNaSXoyWWNibm95CnBiNis1enRtVmJrblZWSlVWeGlzYmJDN3o2bVhOU3FSeTg1UU10eHZkMy80QUErbkRSVGpvb3Y2MHJLeVNDTmoKcjF1cG00M1Raa0dWWmVxbjdrWkZ0dDFuSkFHTENVTEE2eXhYZndJREFRQUJBb0lCQURMbjNGM2VnUStURDZmSwppRDU5K0NKbEsyOHh6SkV5RVJ3MHVEY2x0NlJnSkFnblRhQlpENkpEUWJnckN2S2xZTnF6THFDdGFpejR1bTN5CmJsalJJc1J3bW56bk9sUGE5N05JQWlWZlp3c2xJZzhsbS9NM1lHY1NncjdXQUo2RjhDb0tmUUVNOVozK0ptcEkKN1NrT3FRMTIyV2N4OGdjTFBzaTJxUGZLNjhsRzBORUEyU1E3azk1TjY5bmQzSmVOSjZHTlRsOXNqU1JMTHgyQQpOZ2k4NzRjWW5BbmhITUZ6SGoxWC9UMWpmTm9ncjN1QWYyT2FZQVhqVGgyRjNJSFlCVDBXcnRKbWRERjZ5bjdyClJJNTdlaFpIS3A1TEd4dkN6cWRWV0VoMW5rTGJqTGllM3hmOUkwanZWbDZJQzV6ZmVyTVFGTC9DbWN6a2tvVUMKb3dvbDNza0NnWUVBeHJqMmhPVDBEMHk2OXl6NEszQTRMdkV4Uk11K1N4L05NeGYxR3FXVDlMQUJoMTdyVndiSQplTERndHh2USs0aDU4SkNvcllYYmZrdlFhWUF3dkdhTFB2d1p4UjVOQklZR1FkanRBN1ZTUktkUmZWN1hmQ2tUClpaTTBZMHRaWVJsakQ1SjFoUVBTUlNTQ3dQR0tUMG1sVUNFd2F1bGlIMmtIMTNMYzJiTEFVUTBDZ1lFQXo4RG4KT3NkUlI0OFpqaUppWUMzT043Wnptd0NTRHRUUXVQL0N1bXlDL1o3S3FEQi95YTVWVTZVRUM3UURUWHJVSk9KNwo2V0tLdlR0UGFwQ09LL0t4cVZJdmpPd3plOG96aHIxbUhUZ2hyU2IzK0ZQYytTTTBIR3gvNjNrRkFKcGpjL3JZCi8vd3VZa1lVa29BNExvQUdJeDFIRDBsako0L1U4V0Z4bnlPZTc3c0NnWUEzZzR3NmE4dFNTdGtnRzh2b05wMDcKWDkybm5sTTJvVmV0b2sreWRmYnpNQWY5VFNMcFdOZS8vNG1ISFpvdjZseEdPK01qTm5XdXZuY21RYzFibERucQpDbnNZZWZLQ0JQN1Nma3NYOGh0ZFduQ0ZXVzFSSGhoeEU4SmF5cjNaUmtKS0kxdjVJS3dvN0o0cVVFK1cvcmZUCnZySEw3QTZoUDdJVkdkMWdZM01lTVFLQmdFaVFuWE4zYVJsd2owR0pHd2x6Rm1ONVUvbmhBaU15REZHQjdCZEgKaloxZW1IU1V0QjNTeWJUMG4yd2pEVUJEMWRPb1ZCV1p1TklONUZoWmMzOUFQdlBnWkFGNkF5V0s0K0o5Unl5UApqWnU0VDhhcUxEWE5LUWVBa2xIQ0xKQWdYUHFHdE1MODU0cWw1Y3VpQTMyaWRBSlIySjhyUi9ucWtEdGJpTjlJCkJ0eS9Bb0dCQUwxbThPd0pvb0tBQ1hUUXk3NjBjNnZDWml4VEhLRGxoMnJRRDlscGlpNFFWeUIwQVJLTGJzS2UKQml1d1VPR2cvL2dyUDV6a1NBUkRvSE5iVTVRS2dIV0ZmdzhvN3pwZFlia0MrYTlRVEtqb3I3aU5BZno0WUlWbQowck1YSm1qdmpIODdhcUpwR2NJVkxOUHdhTUdabUQ0SzZqdklmMVJsa3lZaEorZXpzWlFJCi0tLS0tRU5EIFJTQSBQUklWQVRFIEtFWS0tLS0tCg==";
//        String clientCertData = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSURFekNDQWZ1Z0F3SUJBZ0lJTTZlZ2RHdFozMWN3RFFZSktvWklodmNOQVFFTEJRQXdGVEVUTUJFR0ExVUUKQXhNS2EzVmlaWEp1WlhSbGN6QWVGdzB5TVRBNE16RXdNek15TVRkYUZ3MHlNakE0TXpFd016TXlNVGxhTURReApGekFWQmdOVkJBb1REbk41YzNSbGJUcHRZWE4wWlhKek1Sa3dGd1lEVlFRREV4QnJkV0psY201bGRHVnpMV0ZrCmJXbHVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQW9VVk5qTi9mczlKTllPa0wKZ2t4V0ZaOXNpNEZnZVkzb01hdTdaRXhEbDh6cjNxRUZEcGs1M24vdWhQMkd4WHVvQ2o4ejZUc05Jbk5hQXR0YwppdkJ3aUVQTklnbzJVSWI2NnIrbjI2KzNkd2xibzhjcHhvd2FxVUF2dS84bTJmUTZRc2g4bGNMRzgzWVpVeENRCkt1bjhnd1pDazk5THZBRFFpcXFmR1B2TXVJWVRLZHV5UDBUaDlyZmY3Y3h6S0R6RUt5T2hMWHNXeUZUQkgzNWIKZjdHbUVQbGtuaGpVZkNRRTJGcFVlQ1pJejJZY2Jub3lwYjYrNXp0bVZia25WVkpVVnhpc2JiQzd6Nm1YTlNxUgp5ODVRTXR4dmQzLzRBQStuRFJUam9vdjYwckt5U0NOanIxdXBtNDNUWmtHVlplcW43a1pGdHQxbkpBR0xDVUxBCjZ5eFhmd0lEQVFBQm8wZ3dSakFPQmdOVkhROEJBZjhFQkFNQ0JhQXdFd1lEVlIwbEJBd3dDZ1lJS3dZQkJRVUgKQXdJd0h3WURWUjBqQkJnd0ZvQVVzaU9PMHg1R0k0OGR6Y1BRRWs3UDdHSHNxczR3RFFZSktvWklodmNOQVFFTApCUUFEZ2dFQkFMWCszTE9XMnNlak1yQUkxeDUvaldTU0ZoQ1lQVGxORTJ5WStxdnFYN0dReXZMYkZTYjR6bzkzCmE3bzZjcjFxS00wOVYwcEZidHBwQStDMjNMZ2Rxa01GbzZjQ0xZVGZLdXR2TitObnpOZytBejI4TXE2NUt2VWYKbjNHTkd0V1VJZm1kTUxHb3ljM2NVV3dCcG14VDVSRWo0TFRvQVFmVGw3WjVJSmY0c1YvckZKd3pXSUNzcXgrVQphUkEyazVyOWxsVXAzVWV0OHZ2RDZRYWo3VFkyNWVZTWFsTkFBa1FWVmxBTVBrY0tTUkNpck0xRDU4NDkwYXRsCitNWURYcHpoUlFONmhFS2J5WEppMS8zZWlSQnR0aGhndnQ4UVREZU1RQ3phZ05vYlg0eDgxUkVKYWdMNEh0WjQKQ09IM3FnWXRtdExZWEU4WENBSjhnWThKQzFRdm5Rcz0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQo=";

        Config config = Config.autoConfigure(null);
        Config build = new ConfigBuilder(config)
            .withMasterUrl("https://" + address)
            .withApiVersion("v1")
            .withCaCertData(k8sUser.caCertData)
            .withClientKeyData(k8sUser.clientKeyData)
            .withClientCertData(k8sUser.clientCertData) //
            .withClientKeyPassphrase("changeit") //
            .withNamespace("default")   // for instance

            .withConnectionTimeout(10000) //
            .withRequestTimeout(10000) //
            .withRollingTimeout(900000) //

            .withWatchReconnectInterval(1000) //
            .withWatchReconnectLimit(-1) //
            .withLoggingInterval(20000) //

            .withTrustCerts(true) //
            .withHttp2Disable(false) //
            .build();
        return build;
    }

    private static String buildMasterUrl(URL url) {
        return (url.getParameter(USE_HTTPS, true) ?
                "https://" : "http://")
                + url.getHost() + ":" + url.getPort();
    }

    private static void initAuthentification(K8sUser user){
        Properties properties = new Properties();
        try {
            FileInputStream in = new FileInputStream(K8SUSERKEYS_PATH);
            properties.load(in);
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        user.caCertData = properties.getProperty("certificate-authority-data");
        user.clientKeyData = properties.getProperty("client-key-data");
        user.clientCertData = properties.getProperty("client-certificate-data");
    }

    private static String decodeBase64(String str) {
        return StringUtils.isNotEmpty(str) ?
                new String(Base64.getDecoder().decode(str)) :
                null;
    }

    class K8sUser {
        String caCertData = "";
        String clientKeyData = "";
        String clientCertData = "";
    }
}
