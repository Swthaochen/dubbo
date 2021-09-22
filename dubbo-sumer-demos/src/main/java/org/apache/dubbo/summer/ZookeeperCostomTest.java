package org.apache.dubbo.summer;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.demo.api.DemoService;

public class ZookeeperCostomTest {
    private static final String zookeeperHost = System.getProperty("zookeeper.address", "10.100.2.180");
    private static final String zookeeperPort = System.getProperty("zookeeper.port","8801");

    public static void main(String[] args) {
        ReferenceConfig<DemoService> reference = new ReferenceConfig<>();
        reference.setApplication(new ApplicationConfig("api-dubbo-consumer"));
        RegistryConfig registryConfig = new RegistryConfig(
            "zookeeper://" + zookeeperHost + ":" + zookeeperPort);
        registryConfig.setTimeout(10000);
        reference.setRegistry(registryConfig);
        reference.setInterface(DemoService.class);
        DemoService service = reference.get();
        String message = service.sayHello("dubbo3.0");
        System.out.println(message);
    }
}
