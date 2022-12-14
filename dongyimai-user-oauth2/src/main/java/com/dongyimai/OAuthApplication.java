package com.dongyimai;

import feign.RequestTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.offcn.user.feign"})
@MapperScan(basePackages = "com.offcn.auth.dao")
public class OAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OAuthApplication.class,args);
    }

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    @Bean
    public RequestTemplate requestTemplate(){
        return new RequestTemplate();
    }
}