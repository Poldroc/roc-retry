package com.poldroc.retry.annotation.annotation.metadata;

import com.poldroc.retry.annotation.handler.RetryAbleHandler;

import java.lang.annotation.*;
/**
 * 可重试注解
 * 1.用于注解之上用于指定重试信息
 * @see com.poldroc.retry.api.context.RetryContext
 * @author Poldroc
 *  
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Inherited
public @interface RetryAble {

    /**
     * 对应的注解处理器
     * @return class 信息
     */
    Class<? extends RetryAbleHandler> value();

}
