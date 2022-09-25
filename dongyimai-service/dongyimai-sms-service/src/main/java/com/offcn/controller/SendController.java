package com.offcn.controller;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class SendController {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //发送方，生产方
    @RequestMapping("/sendSms")
    public void send() {
        Map<String, String> map = new HashMap<>();
        map.put("mobile", "17807305335"); // 替换目的手机号
        map.put("code", "520520");
        rabbitTemplate.convertAndSend("dongyimai.sms.queue", map);//发送消息到指定队列
    }
}