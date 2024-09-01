package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 根据结果进行重试的条件
 *
 * @author Poldroc
 * @since 2024/7/12
 */
@ThreadSafe
public abstract class AbstractResultRetryCondition<R> implements RetryCondition<R> {
    @Override
    public boolean condition(RetryAttempt<R> retryAttempt) {
        return resultCondition(retryAttempt.result());
    }

    /**
     * 对结果进行判断
     *
     * @param result 结果信息
     * @return 对结果进行判断
     */
    protected abstract boolean resultCondition(final R result);


    /**
     * 判断是否有结果信息
     * 1. 有，返回 true
     * 2. 无，返回 false
     *
     * @param result 返回对象
     * @return 是否有结果
     */
    protected boolean hasResult(final R result) {
        return result != null;
    }

}
