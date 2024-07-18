package com.poldroc.retry.annotation.handler;

import com.poldroc.retry.api.context.RetryWaitContext;

import java.lang.annotation.Annotation;
/**
 * 重试等待处理器
 * @author Poldroc
 * @date 2024/7/15
 */

public interface RetryWaitAbleHandler<A extends Annotation, T> {

    /**
     * 根据注解信息构建上下文
     * @param annotation 可重试等待注解
     * @return 重试等待上下文
     */
    RetryWaitContext<T> build(final A annotation);

}
