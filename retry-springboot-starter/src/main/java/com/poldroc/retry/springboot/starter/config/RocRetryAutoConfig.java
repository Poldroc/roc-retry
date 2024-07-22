package com.poldroc.retry.springboot.starter.config;

import com.poldroc.retry.spring.annotation.EnableRetry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * 重试自动配置类
 * @author Poldroc
 * @date 2024/7/22
 */
@EnableRetry
@Configuration
@ConditionalOnClass(EnableRetry.class)
public class RocRetryAutoConfig {
}
