package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 根据异常进行重试的条件
 *
 * @author Poldroc
 *  
 */
@ThreadSafe
public abstract class AbstractCauseRetryCondition implements RetryCondition {
    @Override
    public boolean condition(RetryAttempt retryAttempt) {
        return causeCondition(retryAttempt.cause());
    }

    /**
     * 对异常信息进行判断
     * 1. 用户可以判定是否有异常
     *
     * @param throwable 异常信息
     * @return 对异常信息进行判断
     */
    protected abstract boolean causeCondition(final Throwable throwable);

    /**
     * 判断是否有异常信息
     * 1. 有，返回 true
     * 2. 无，返回 false
     *
     * @param throwable 异常信息
     * @return 是否有异常信息
     */
    protected boolean hasException(final Throwable throwable) {
        return throwable != null;
    }
}
