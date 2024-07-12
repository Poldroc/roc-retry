package com.poldroc.retry.core.support.wait;

import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 递增重试等待策略
 *
 * @author Poldroc
 * @date 2024/7/11
 */
@ThreadSafe
public class IncreaseRetryWait extends AbstractRetryWait {
    @Override
    public WaitTime waitTime(RetryWaitContext retryWaitContext) {
        int previousAttempt = retryWaitContext.attempt() - 1;
        // 结果为重试等待时间的值加上重试次数减一乘以重试等待时间的因子，然后四舍五入
        long result = Math.round(retryWaitContext.value() + previousAttempt * retryWaitContext.factor());
        return super.rangeCorrect(result, retryWaitContext.min(), retryWaitContext.max());
    }
}
