package org.apache.dubbo.summer.provider;


import org.apache.dubbo.demo.api.DemoService;

public class DemoServiceImpl implements DemoService {
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
