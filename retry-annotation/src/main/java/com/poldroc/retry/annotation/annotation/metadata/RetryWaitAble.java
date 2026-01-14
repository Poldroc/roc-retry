package com.poldroc.retry.annotation.annotation.metadata;

import com.poldroc.retry.annotation.handler.RetryWaitAbleHandler;

import java.lang.annotation.*;

/**
 * 可重试等待的注解
 * 1. 用于注解之上指定等待注解的处理信息
 * @see com.poldroc.retry.api.context.RetryWaitContext
 * @author Poldroc
 *  
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Inherited
public @interface RetryWaitAble {

    /**
     * 对应的注解处理器
     * @return class 信息
     */
    Class<? extends RetryWaitAbleHandler> value();

}
