package com.offcn.user.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
//作用获取公钥，校验令牌是否合法；HTTP安全配置，需要对"/user/add","/user/load/*进行放行，因为申请令牌需要工具注册在用户表中的用户才能注册
@Configuration
@EnableResourceServer //开启资源校验 校验令牌
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)//激活方法上的PreAuthorize注解，默认关闭，一个是开启注解，一个是开启验证
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    //公钥，
    private static final String PUBLIC_KEY = "public.key";

    /***
     * 定义JwtTokenStore
     * @param jwtAccessTokenConverter
     * @return
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }

    /***
     * 定义JJwtAccessTokenConverter
     * @return
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setVerifierKey(this.getPubKey()); //校验公钥
        return converter;
    }
    /**
     * 获取非对称加密公钥 Key
     * @return 公钥 Key
     */
    private String getPubKey() {
        Resource resource = new ClassPathResource(PUBLIC_KEY);//通过类路径资源获取公钥配置文件
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            return br.lines().collect(Collectors.joining("\n"));//自动加换行
        } catch (IOException ioe) {
            return null;
        }
    }

    /***
     * Http安全配置，对每个到达当前模块的http请求链接进行校验，只有登录了才能请求获取用户信息
     * @param http
     * @throws Exception
     *   //所有请求必须认证通过
     *         //配置地址放行，用户注册,/user/load/*为用户认证时，通过查询到数据库有这个注册过的用户的用户名，才去授权认证发放令牌
     *         //不写会报在流模式下不能进行身份认证，数据库detail的两个用户是官网和第三方的注册用户
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        //所有请求必须认证通过
        http.authorizeRequests()
                //下边的路径放行
                .antMatchers(
                        "/user/add","/user/load/*"). //配置地址放行
                permitAll()
                .anyRequest().
                authenticated();    //其他地址需要认证授权
    }
}