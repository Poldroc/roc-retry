package com.poldroc.retry.annotation.handler.impl;

import com.poldroc.retry.annotation.annotation.RetryWait;
import com.poldroc.retry.annotation.handler.RetryWaitAbleHandler;
import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.core.core.RetryWaiter;
/**
 * 默认的重试等待处理器
 * @author Poldroc
 * @since 2024/7/17
 */
@ThreadSafe
public class DefaultRetryWaitAbleHandler<R> implements RetryWaitAbleHandler<RetryWait, R> {
    @Override
    public RetryWaitContext<R> build(RetryWait annotation) {
        return RetryWaiter
                .<R>retryWait(annotation.retryWait())
                .min(annotation.min())
                .max(annotation.max())
                .factor(annotation.factor())
                .value(annotation.value())
                .context();
    }
}
