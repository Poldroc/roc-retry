package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.common.annotation.ThreadSafe;

import java.util.LinkedList;

/**
 * 重试条件初始化类
 * 满足任意一个条件即可
 *
 * @author Poldroc
 * @date 2024/7/12
 */
@ThreadSafe
public abstract class AbstractRetryConditionInit implements RetryCondition {
    @Override
    public boolean condition(RetryAttempt retryAttempt) {
        LinkedList<RetryCondition> conditions = new LinkedList<>();
        this.init(conditions, retryAttempt);
        // 判断
        for (RetryCondition condition : conditions) {
            if (condition.condition(retryAttempt)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 初始化列表
     *
     * @param pipeline     当前列表泳道
     * @param retryAttempt 执行信息
     */
    protected abstract void init(final LinkedList<RetryCondition> pipeline,
                                 final RetryAttempt retryAttempt);

}
