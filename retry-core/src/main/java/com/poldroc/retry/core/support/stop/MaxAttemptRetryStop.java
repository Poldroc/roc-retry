package com.poldroc.retry.core.support.stop;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.stop.RetryStop;
/**
 * 最大尝试次数终止策略
 * @author Poldroc
 * @since 2024/7/11
 */

public class MaxAttemptRetryStop implements RetryStop {

    /**
     * 最大尝试次数
     */
    private final int maxAttempt;

    public MaxAttemptRetryStop(int maxAttempt) {
        if (maxAttempt <= 0) {
            throw new IllegalArgumentException("MaxAttempt must be positive");
        }
        this.maxAttempt = maxAttempt;
    }

    @Override
    public boolean stop(RetryAttempt attempt) {
        return attempt.attempt() >= maxAttempt;
    }
}
