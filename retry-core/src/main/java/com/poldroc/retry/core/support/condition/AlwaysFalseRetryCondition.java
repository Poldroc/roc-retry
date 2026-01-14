package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 恒为假重试条件
 *
 * @author Poldroc
 *  
 */

@ThreadSafe
public class AlwaysFalseRetryCondition implements RetryCondition {

    /**
     * 内部静态类
     */
    private static class AlwaysFalseRetryConditionHolder {
        private static final AlwaysFalseRetryCondition INSTANCE = new AlwaysFalseRetryCondition();
    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static RetryCondition getInstance() {
        return AlwaysFalseRetryConditionHolder.INSTANCE;
    }

    @Override
    public boolean condition(RetryAttempt retryAttempt) {
        return false;
    }

}
