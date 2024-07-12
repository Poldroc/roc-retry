package com.poldroc.retry.common.annotation;

import java.lang.annotation.*;
/**
 * 线程不安全安全注解
 * @author Poldroc
 * @date 2024/7/11
 */

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotThreadSafe {
}
