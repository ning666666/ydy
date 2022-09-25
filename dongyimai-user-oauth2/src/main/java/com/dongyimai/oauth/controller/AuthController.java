package com.dongyimai.oauth.controller;

import com.dongyimai.oauth.service.AuthService;
import com.dongyimai.oauth.util.AuthToken;
import com.dongyimai.oauth.util.CookieUtil;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/user")
public class AuthController {

    //客户端ID
    @Value("${auth.clientId}")
    private String clientId;

    //秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    //Cookie存储的域名
    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    //Cookie生命周期
    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Autowired
    AuthService authService;

    @PostMapping("/login")
    public Result login(String username, String password, String from) {

        if (StringUtils.isEmpty(username)) {
            throw new RuntimeException("用户名不允许为空");
        }
        if (StringUtils.isEmpty(password)) {
            throw new RuntimeException("密码不允许为空");
        }
        //申请令牌
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);


        //用户身份令牌
        String access_token = authToken.getAccessToken();

        //将令牌存储到cookie
        saveCookie(access_token, from);

        return new Result(true, StatusCode.OK, "登录成功！:" + from);
    }

    /***
     * 将令牌存储到cookie
     * @param token
     */
    private void saveCookie(String token, String from) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response, cookieDomain, "/", "Authorization", token, cookieMaxAge, false);
        // 5秒之后跳转from参数所指向的页面
        response.setHeader("Refresh", "5;URL=" + from);
        //也可以使用如下方式返回到原请求的资源路径
//        response.setStatus(303);
//        response.setHeader("Location",from);

    }
}
