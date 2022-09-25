package com.offcn.seckill;

import com.offcn.utils.IdWorker;
import com.offcn.utils.TokenDecode;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.offcn.seckill.dao"})
@EnableScheduling//开启定时任务
@EnableAsync //开启spring异步操作
public class SeckillApplication {


    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class, args);
    }

    @Bean //需要做订单
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }
}