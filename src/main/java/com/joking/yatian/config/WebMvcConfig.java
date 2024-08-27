package com.joking.yatian.config;

import com.joking.yatian.controller.interceptor.LoginInterceptor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Joking7
 * @ClassName WebMvcConfig
 * @description: TODO
 * @date 2024/8/5 下午10:11
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns("/login","/getKaptcha","/register","/activation")
                .addPathPatterns("/**");
    }
}
