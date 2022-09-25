package com.offcn.utils;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 要调用的话，需要在主启动类中声明，并在方法上注入
 * 单例模式，自己实例化单个实例，别人不能实例化，对外提供公共的访问方式
 */
@Component
public class TokenDecode {
    //公钥
    private static final String PUBLIC_KEY = "public.key";

    private static String publickey = "";


    /**
     * 获取非对称加密公钥 Key
     *
     * @return 公钥 Key
     */
    public String getPubKey() {
        if (!StringUtils.isEmpty(publickey)) {
            return publickey;
        }
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            publickey = br.lines().collect(Collectors.joining("\n"));
            return publickey;
        } catch (IOException ioe) {
            return null;
        }
    }

    /***
     * 读取令牌数据
     */
    public Map<String, String> dcodeToken(String token) {
        //解析校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubKey()));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        return JSON.parseObject(claims, Map.class);
    }

    /***
     * 根据令牌获取用户信息
     *
     * @return
     */
    public Map<String, String> getUserInfo() {
        //获取授权信息，用户登录后的信息都封装在这里面
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        //令牌解码
        return dcodeToken(details.getTokenValue());
    }
}
