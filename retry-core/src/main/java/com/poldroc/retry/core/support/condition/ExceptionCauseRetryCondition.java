package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.common.annotation.ThreadSafe;

/**
 * 有异常则触发重试
 *
 * @author Poldroc
 * @since 2024/7/12
 */
@ThreadSafe
public class ExceptionCauseRetryCondition extends AbstractCauseRetryCondition {
    @Override
    protected boolean causeCondition(Throwable throwable) {
        return hasException(throwable);
    }
}
