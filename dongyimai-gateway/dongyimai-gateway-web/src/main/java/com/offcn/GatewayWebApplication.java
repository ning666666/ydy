package com.offcn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
 //这里的包结构不同之前的controller，因为controller也是给我们匹配服务器资源名的，也就是拦截请求的路径的，
 //而gateway模块的配置文件也配置了路径的拦截，所以不能同之前的controller
@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {
    public static void main(String[] args){
        SpringApplication.run(GatewayWebApplication.class,args);
    }
    /***
     * IP限流
     *@return
     */
    @Bean(name="ipKeyResolver")
    public KeyResolver userKeyResolver(){
        //KeyResolver用于计算某一个类型的限流的KEY也就是说，可以通过KeyResolver来指定限流的Key。
        //可以根据IP来限流，比如每个IP每秒钟只能请求一次，在dongyimai-gateway-web
        //模块的启动类GatewayWebApplication定义key的获取，获取客户端IP，将IP作为key
        return new KeyResolver(){
            @Override
            public Mono<String> resolve(ServerWebExchange exchange){
                //获取远程客户端IP
                String hostName = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                System.out.println("hostName:"+hostName);
                return Mono.just(hostName);
            }
        };
    }
}
