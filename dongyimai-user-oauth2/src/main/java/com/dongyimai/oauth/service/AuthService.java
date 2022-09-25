package com.dongyimai.oauth.service;

import com.dongyimai.oauth.util.AuthToken;

public interface AuthService {

    /***
     * 授权认证方法
     */
    AuthToken login(String username, String password, String clientId, String clientSecret);
}
