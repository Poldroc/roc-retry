package com.poldroc.retry.core.support.condition;


import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 恒为真重试条件
 *
 * @author Poldroc
 * @date 2024/7/12
 */

@ThreadSafe
public class AlwaysTrueRetryCondition implements RetryCondition {

    /**
     * 内部静态类
     */
    private static class AlwaysTrueRetryConditionHolder {
        private static final AlwaysTrueRetryCondition INSTANCE = new AlwaysTrueRetryCondition();
    }

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static RetryCondition getInstance() {
        return AlwaysTrueRetryConditionHolder.INSTANCE;
    }

    @Override
    public boolean condition(RetryAttempt retryAttempt) {
        return true;
    }

}
