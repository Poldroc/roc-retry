package com.poldroc.retry.api.core;

import com.poldroc.retry.api.context.RetryContext;
/**
 * 重试接口
 * @author Poldroc
 * @date 2024/7/11
 */

public interface Retry<R> {


    /**
     * 执行重试
     * @param context 执行上下文
     * @return 执行结果
     */
    R retryCall(final RetryContext<R> context);
}
