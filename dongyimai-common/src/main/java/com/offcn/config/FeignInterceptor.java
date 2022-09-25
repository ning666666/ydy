package com.offcn.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * FeignInterceptor拦截器，拦截feign的远程服务调用，先登录了才能做微服务之间的feign远程服务调用
 * 作用：
 * 由于微服务之间的调用也是需要做令牌的校验，验证用户是否登录，是否有权限访问服务器资源，也就是fegin的远程服务调用，
 * feign的远程调用也是HTTP（rest风格）方式的进行的远程服务调用，但是header头文件中是不携带令牌的，
 * 且微服务之间不会传递头文件，所以我们需要配置拦截器，在feign远程调用之前，先检查下头文件中是否带了令牌，
 * 不管带不带，再将请求头文件(网关会将令牌交给oauth校验以后再放进头文件)中的令牌数据再放入到header中，再调用其他微服务；
 */
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            //使用RequestContextHolder工具获取request相关变量
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                //取出request
                HttpServletRequest request = attributes.getRequest();
                //获取所有头文件信息的key
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        //头文件的key
                        String name = headerNames.nextElement();
                        //头文件的value
                        String values = request.getHeader(name);
                        //将令牌数据添加到头文件中
                        requestTemplate.header(name, values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
