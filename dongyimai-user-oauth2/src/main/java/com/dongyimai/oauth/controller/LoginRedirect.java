package com.dongyimai.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/oauth")
public class LoginRedirect {


    // http://localhost:9100/oauth/login?from =http://localhost:9008/cart/addToCart?itemId=1369368&num=10
    @RequestMapping("/login")
    public String login(@RequestParam(value = "from",required = false,defaultValue = "") String from, Model model){
        // 存储from
        model.addAttribute("from",from);
        return "login";
    }
 /*   *//***
     * 跳转到登录页面
     * @return
     *//*
    @GetMapping(value = "/login")
    public String login(){
        return "login";
    }*/
}
