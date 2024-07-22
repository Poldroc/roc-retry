package com.poldroc.retry.spring.annotation;

import com.poldroc.retry.spring.config.RetryAopConfig;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(RetryAopConfig.class)
@EnableAspectJAutoProxy
public @interface EnableRetry {
}
