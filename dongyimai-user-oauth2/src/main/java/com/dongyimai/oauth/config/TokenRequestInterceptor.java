package com.dongyimai.oauth.config;

import com.dongyimai.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {
    /**
     * feign执行之前进行拦截，feign执行之前发放令牌
     * 作用是：因为第三方认证服务器发放令牌时需要对该用户做授权认证服务，也就是已经在官网注册的用户才能发放令牌，
     * 那么做授权认证的时候需要feign远程调用用户微服务查询用户信息，是否在官网注册过，我们这里做的目的是发放
     * 管理员令牌，也就是自己发放令牌而不借助第三方，在feign远程调用执行之前就发放令牌，意思就是发放的管理员令牌是
     * 不需要经过数据库已注册过的用户就能发放的，管理员权限太大，在分布式应用中不友好
     * @param template
     */
    @Override
    public void apply(RequestTemplate template) {
        String token = AdminToken.createAdminToken();
        template.header("Authorization","bearer " +token);
    }
}
