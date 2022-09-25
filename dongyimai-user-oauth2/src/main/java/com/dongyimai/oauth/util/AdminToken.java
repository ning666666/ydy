package com.dongyimai.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.util.Map;

public class AdminToken {

    /**
     * 发放管理员令牌，根据公私钥发，还配置拦截器发放
     */
    public static String createAdminToken(){
        // 加载私钥
        ClassPathResource resource = new ClassPathResource("dongyimai.jks");
        // 读取私钥内容
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(resource, "dongyimai".toCharArray());
        // keyPair 秘钥对
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("dongyimai", "dongyimai".toCharArray());
        RSAPrivateCrtKey privateCrtKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        // 创建令牌（管理员 拥有最大权限）  jwt  需要私钥
        //   头部 载荷 签名
        Map<String ,Object> payload = (Map<String, Object>) keyPair.getPrivate();
        payload.put("name","xiaobai");
        payload.put("address","cs");
        payload.put("authorities",new String[]{"admi","user"});
        Jwt jwt = JwtHelper.encode(JSON.toJSONString(payload), new RsaSigner(privateCrtKey));
        // jwt中获取令牌
        String token = jwt.getEncoded();
        return token;
    }
}
