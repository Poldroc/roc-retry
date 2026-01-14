package com.poldroc.retry.core.support.wait;

import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.common.annotation.ThreadSafe;

@ThreadSafe
public class RandomRetryWait extends AbstractRetryWait {

    @Override
    public WaitTime waitTime(RetryWaitContext retryWaitContext) {
        long min = retryWaitContext.min();
        long max = retryWaitContext.max();
        long result;
        if (max <= min) {
            result = min;
        } else {
            result = min + (long) (Math.random() * (max - min));
        }
        return super.rangeCorrect(result, min, max);
    }
}
