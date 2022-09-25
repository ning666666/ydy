package com.offcn.sellergoods;

import com.offcn.config.FeignInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
/*我们首先学习的三个框架SSM，整合他们三个还是比较麻烦的，后面我们学到的SpringBoot是集成了Spring和SpringMVC的
以及我们学到的MybatisPlus集成了Mybatis不用再去写sql语句，更加方便使用，所以我们在整合SpringBoot和mybatis时就
只剩下mybati中数据库的数据源配置和接口映射文件、实体类起别名、setting中的自动驼峰命名映射、懒加载、二级缓存等等，
在配置接口映射文件时，还需要在实体类对应的Mapper接口打上@Mapper注解，方便我们找到对应的mapper接口，利用动态代理
模式反射创建接口的实现类对象，但是每次创建的代理类又是不一样的，我们是针对接口的代理生成接口实现类对象，如果注解打
在接口的实现类上，可能导致找不到对应的接口实现类，也就执行不了对应的sql语句对数据库进行增删改查，完成我们的业务逻辑，
但是我们的写法不是把@Mapper写在接口上，而是在启动类中添上@MapperScan包扫描的注解，可以把mapper包上的所有@Mapper
注解都打上，也就不用每个mapper接口上都打上这个注解，我们是把配置写到application.yml配置文件中，但还能简化，所以
我们在配置SpringBoot和MybatisPlus时就只剩下数据源配置和接口映射文件的配置，而接口映射文件的类路径配置也是可以
省略不写的，所以yml配置文件中的MybatisPlus的配置信息也可以不写了。
*/
@MapperScan("com.offcn.sellergoods.dao")
public class SellerGoodsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SellerGoodsApplication.class);
    }
    /***
     * 创建拦截器Bean对象，将头文件中令牌数据解析完，再次加入到feign调用的头文件中
     * @return
     */
    @Bean
    public FeignInterceptor feignInterceptor(){
        return new FeignInterceptor();
    }
}
