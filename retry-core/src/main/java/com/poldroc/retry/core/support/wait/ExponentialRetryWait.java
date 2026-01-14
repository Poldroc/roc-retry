package com.poldroc.retry.core.support.wait;

import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.model.WaitTime;

/**
 * 指数增长的重试等待策略
 * <p>
 * 1. factor 为重试等待时间的因子
 * 2. factor 为 1 时，等待时间为固定值
 * 3. factor 大于 1 时，等待时间会逐渐增大
 * 4. factor 小于 1 且 大于 0 时，等待时间会逐渐减小
 *
 * @author Poldroc
 *  
 */

public class ExponentialRetryWait extends AbstractRetryWait {
    @Override
    public WaitTime waitTime(RetryWaitContext retryWaitContext) {
        int previousAttempt = retryWaitContext.attempt() - 1;
        double pow = Math.pow(retryWaitContext.factor(), previousAttempt);
        long result = Math.round(retryWaitContext.value() * pow);
        return super.rangeCorrect(result, retryWaitContext.min(), retryWaitContext.max());
    }
}
