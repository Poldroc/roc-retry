package com.poldroc.retry.annotation.handler;

import com.poldroc.retry.api.context.RetryContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.Callable;
/**
 * 可重试注解处理器
 * @author Poldroc
 * @since 2024/7/13
 */

public interface RetryAbleHandler<A extends Annotation, T> {

    /**
     * 根据注解信息构建上下文
     * @param annotation 可重试注解
     * @param callable 待重试方法
     * @return 重试上下文
     */
    RetryContext<T> build(final A annotation,
                          final Callable<T> callable);

}
