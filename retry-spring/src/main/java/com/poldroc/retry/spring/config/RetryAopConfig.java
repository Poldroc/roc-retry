package com.poldroc.retry.spring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 重试配置类
 * @author Poldroc
 * @since 2024/7/22
 */

@Configuration
@ComponentScan(basePackages = "com.poldroc.retry.spring")
public class RetryAopConfig {
}
