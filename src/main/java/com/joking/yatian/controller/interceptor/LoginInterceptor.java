package com.joking.yatian.controller.interceptor;

import com.joking.yatian.service.UserService;
import com.joking.yatian.util.HostHolder;
import com.joking.yatian.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * LoginInterceptor JWT登录验证拦截器, 用于处理基于JWT的认证。
 * @author Joking7
 * @version 2024/07/18 
**/
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private HostHolder hostHolder;


}