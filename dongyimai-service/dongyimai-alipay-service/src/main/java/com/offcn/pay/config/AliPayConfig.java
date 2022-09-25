package com.offcn.pay.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by travelround on 2021/6/25.通过配置类读取配置文件中的参数
 */
@Configuration
public class AliPayConfig {
    @Value("${alipay.appId}")
    private String appId;
    @Value("${alipay.serverUrl}")
    private String serverUrl;
    @Value("${alipay.privateKey}")
    private String privateKey;
    @Value("${alipay.alipayPublicKey}")
    private String alipayPublicKey;
    @Value("${alipay.format}")
    private String format;
    @Value("${alipay.charset}")
    private String charset;
    @Value("${alipay.signType}")
    private String signType;
    @Bean
    public AlipayClient getPayClient() {
        return new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset, alipayPublicKey, signType);
    }
}
