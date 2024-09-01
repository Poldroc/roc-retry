package com.poldroc.retry.api.support.condition;

import com.poldroc.retry.api.model.RetryAttempt;

/**
 * 重试执行的条件
 * @author Poldroc
 * @since 2024/7/11
 */
public interface RetryCondition<R> {

    /**
     * 判断是否满足重试条件
     * @param retryAttempt 重试相关信息
     * @return 是否满足条件
     */
    boolean condition(final RetryAttempt<R> retryAttempt);
}
