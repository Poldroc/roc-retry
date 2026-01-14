package com.poldroc.retry.core.support.condition;
/**
 * 空结果则触发重试
 * @author Poldroc
 *  
 */

public class NullResultRetryCondition<R> extends AbstractResultRetryCondition<R> {

    @Override
    protected boolean resultCondition(R result) {
        return !hasResult(result);
    }
}
