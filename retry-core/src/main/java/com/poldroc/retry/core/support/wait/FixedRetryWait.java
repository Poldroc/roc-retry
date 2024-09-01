package com.poldroc.retry.core.support.wait;

import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 固定时间间隔等待
 *
 * @author Poldroc
 * @since 2024/7/11
 */

@ThreadSafe
public class FixedRetryWait extends AbstractRetryWait {

    @Override
    public WaitTime waitTime(RetryWaitContext retryWaitContext) {
        return super.rangeCorrect(retryWaitContext.value(), retryWaitContext.min(), retryWaitContext.max());
    }

}
