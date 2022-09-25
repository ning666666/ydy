package com.offcn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaServer
public class EurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaApplication.class, args);
    }
    @Bean
    public Marker eurekaServerMarkerBean(){
        return new Marker();
    }
    class Marker{

    }
    //服务怎么注入上去的，如何注册的？
    /*
    启动类中注入@EnableEurekaServer，在erreka中的有一个EurekaServiceInstance类，其中通过其中的属性InstanceInfo类，
    这个类中有一个PortWrapper类，有许多被volatile修饰的属性，表示可见的。

    我的猜想是首先我们在启动类上注入@EnableEurekaServer注解，同时在配置文件中配置了Eureka作为服务器同时也是客户端的一些配置，
    信息，那这些配置信息都是通过EurekaServerConfigBean这个配置类的属性注入上去的；
    */
}
