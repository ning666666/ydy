package com.offcn.pay;

import com.offcn.config.FeignInterceptor;
import com.offcn.utils.TokenDecode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Created by travelround on 2021/6/25.
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableFeignClients(basePackages = {"com.offcn.order.feign"})
@EnableEurekaClient
public class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class);
    }

    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    @Autowired
    private Environment environment;

    //创建消息队列
    @Bean
    public Queue createQueue(){
        return new Queue(environment.getProperty("mq.pay.queue.order"));
    }
    //创建交换机

    @Bean
    public DirectExchange basicExchanage(){
        return new DirectExchange(environment.getProperty("mq.pay.exchange.order"));
    }

    //绑定

    @Bean
    public Binding basicBinding(){
        return  BindingBuilder.bind(createQueue()).to(basicExchanage()).with(environment.getProperty("mq.pay.routing.key"));
    }



    //创建秒杀队列
    @Bean(name="seckillQueue")
    public Queue createSeckillQueue(){
        return new Queue(environment.getProperty("mq.pay.queue.seckillorder"));
    }

    //创建秒杀交换机

    @Bean(name="seckillExchanage")
    public DirectExchange basicSeckillExchanage(){
        return new DirectExchange(environment.getProperty("mq.pay.exchange.seckillorder"));
    }

    //绑定秒杀

    @Bean(name="SeckillBinding")
    public Binding basicSeckillBinding(){
        return  BindingBuilder.bind(createSeckillQueue()).to(basicSeckillExchanage()).with(environment.getProperty("mq.pay.routing.seckillkey"));
    }
}
