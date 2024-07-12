package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 非空结果重试条件
 *
 * @author Poldroc
 * @date 2024/7/12
 */

@ThreadSafe
public class NotNullResultRetryCondition<R> extends AbstractResultRetryCondition<R> {
    @Override
    protected boolean resultCondition(R result) {
        return hasResult(result);
    }
}
