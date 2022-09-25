package com.offcn.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ES索引库作用：我们搜索商品不能每次都通过从数据库查询出来，这样对数据库的使用太过频繁，数据库压力会很大，并且数据库
 * 本身就很脆弱，例如广告层，网页上的广告很多而且是不停的轮播装换的，如果都直接从数据库查，对数据库肯定是不友好的，
 * 这时我们使用Nginx+Redis+数据库的多级缓存来对数据库进行保护，所以这里我们引用ES索引库作为微服务模块，一样是对数据库
 * 的一种保护行为
 */
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.offcn.sellergoods.feign")
@EnableElasticsearchRepositories(basePackages = "com.offcn.search.dao")
/*
注意事项：dao层只是代表数据的流向，也就是数据从哪里过来，我再对它进行增删改查，那这样ES索引库也可以作为数据的流向，
        数据在ES中一样可以像数据库一样对数据进行持久化保存，redis也可以作为数据的流向
*/
/*
    之前我们是需要配置接口的包扫描@MapperScan("com.offcn.sellergoods.dao")给对应包下所有的mapper接口都打上
    @Mapper注解，而ES索引库，也就是检索商品信息的微服务，在ES索引库的启动类上打上
    @EnableElasticsearchRepositories(basePackages = "com.offcn.search.dao")，代替之前的操作
*/
public class SearchApplication {

    public static void main(String[] args) {
        /**
         * Springboot整合Elasticsearch 在项目启动前设置一下的属性，防止报错
         * 解决netty冲突后初始化client时还会抛出异常
         * availableProcessors is already set to [12], rejecting [12]
         ***/
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        SpringApplication.run(SearchApplication.class,args);
    }
}