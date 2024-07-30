package com.joking.yatian.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Joking7
 * @ClassName ThreadPoolConfig
 * @description: 线程池配置
 * @date 2024/7/30 上午2:10
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {
}
