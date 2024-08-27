package com.joking.yatian.controller.interceptor;

import com.joking.yatian.entity.User;
import com.joking.yatian.service.UserService;
import com.joking.yatian.util.HostHolder;
import com.joking.yatian.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

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

    @Value("${token.yangToken}")
    private String yangTime;

    @Value("${token.oldToken}")
    private String oldTime;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        String token = request.getHeader("userToken");
        if(token == null){
            // 清除保存权限验证的结果
            SecurityContextHolder.clearContext();
            response.setHeader("userToken",null);
            response.setHeader("auth","0");
            return false;
        }
        Map<String, String> map = jwtUtil.parseToken(token);
        User user = userService.findUserById(Integer.parseInt(map.get("userId")));

        if(user == null){
            // 清除保存权限验证的结果
            SecurityContextHolder.clearContext();
            response.setHeader("userToken",null);
            response.setHeader("auth","0");
            return false;
        }

        // 自动刷新有效时长
        long timeOfUse = System.currentTimeMillis() - Long.parseLong(map.get("timeStamp"));
        // 年轻token 不用管
        if(timeOfUse < Long.parseLong(yangTime)){
            response.setHeader("userToken",token);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, user.getPassword(), userService.getAuthorities(user.getId()));
            // 存入SecurityContext
            SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        }
        // 年老token 刷新token
        else if(timeOfUse < Long.parseLong(oldTime)){
            response.setHeader("userToken", jwtUtil.getToken(String.valueOf(user.getId()),String.valueOf(user.getType())));
            // 构建用户认证的结果,并存入SecurityContext,以便于Security进行授权.
            // principal: 主要信息; credentials: 证书; authorities: 权限;
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, user.getPassword(), userService.getAuthorities(user.getId()));
            // 存入SecurityContext
            SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        }
        // 过期token 权限置null
        else{
            // 清除保存权限验证的结果
            SecurityContextHolder.clearContext();
            response.setHeader("userToken",null);
            response.setHeader("auth","0");
            return false;
        }
        return true;
    }
}
