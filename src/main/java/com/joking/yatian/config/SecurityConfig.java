package com.joking.yatian.config;

import com.alibaba.fastjson.JSONObject;
import com.joking.yatian.util.CommunityConstant;
import com.joking.yatian.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.SecurityBuilder;
import org.springframework.security.config.annotation.web.WebSecurityConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * SecurityConfig SpringSecurity 配置类
 * @author Joking7
 * @version 2024/07/20
**/
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        // 忽略
        web.ignoring().antMatchers("/test","/login","/getKaptcha","/register","/activation");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        // TODO: 所有异步请求要开启csrf
        http.authorizeRequests()
                .antMatchers(
                        // 已登录用户 包括被封禁账号

                        // 获取论坛主页
                        "/",
                        "/index",
                        // 点赞 关注 取消关注
                        "/like",
                        "/follow",
                        "/unfollow",
                        // 用户主页信息 用户设置页信息
                        "/user/profile/",
                        "/user/setting",
                        // 用户更新头像 更新用户名 更新密码
                        "/user/header/update",
                        "/user/password/update",
                        "/user/header/delete",
                        // 删帖
                        "/discuss/delete",
                        // 获取贴子详细信息
                        "/discuss/detail/",
                        // 查询关注用户的实体 查询用户关注的实体
                        "/followers/",
                        "/followees/",
                        // 获取消息列表 读取消息详细
                        "/notice/list",
                        "/notice/detail/",
                        // 获取私信列表 读取私信详细
                        "/letter/list",
                        "/letter/detail/",
                        // 生成分享长图
                        "/share",
                        // 站内搜索
                        "/search"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_FROZEN
                )
                .antMatchers(
                        // 已登录用户 不包括被封禁账号

                        // 发帖 评论 私信
                        "/discuss/add",
                        "/letter/send",
                        "/comment/add/{discussPostId}"
                )
                .hasAnyAuthority(
                        AUTHORITY_USER,
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR
                )
                .antMatchers(
                        // 允许管理员和版主访问

                        "/test3",
                        // 删贴 置顶 加精
                        "/discuss/deleteByAdmin",
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR,
                        AUTHORITY_ADMIN
                ).antMatchers(
                        // 允许所有人访问

                        // 登录 注册 登录验证码 激活
                        "/test1"
                )
                .permitAll()
                .and().csrf().disable();

        // 权限不够时的处理
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登录
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        //返回结果
                        response.setStatus(403);
                        // 0为未登录 1为权限不足
                        response.setHeader("auth","0");
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        //返回结果
                        response.setStatus(403);
                        // 0为未登录 1为权限不足
                        response.setHeader("auth","1");
                    }
                });
        // 为了执行自己的logout
        // Security底层默认会拦截/logout请求,进行退出处理.
        // 覆盖它默认的逻辑,才能执行我们自己的退出代码.
        // 此处为一个欺骗，程序中没有"/securitylogout"，拦截到这个路径不会处理
        http.logout().logoutUrl("/securitylogout");
    }
}
