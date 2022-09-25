package com.offcn.order;

import com.offcn.config.FeignInterceptor;
import com.offcn.utils.IdWorker;
import com.offcn.utils.TokenDecode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

/**
 * Created by travelround on 2021/7/30.
 */
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.offcn.order.feign","com.offcn.sellergoods.feign","com.offcn.user.feign"})
@MapperScan(basePackages = {"com.offcn.order.dao"})
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public FeignInterceptor getFeignInterceptor() {
        return new FeignInterceptor();
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    //生成订单号
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

}
