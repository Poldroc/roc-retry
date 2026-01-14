package com.poldroc.retry.test.support.conditions;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.core.support.condition.AbstractRetryConditionInit;
import com.poldroc.retry.core.support.condition.RetryConditions;

import java.util.LinkedList;

public class MultipleConditionsCondition extends AbstractRetryConditionInit {
    @Override
    protected void init(LinkedList<RetryCondition> pipeline, RetryAttempt retryAttempt) {
        pipeline.add(RetryConditions.isNullResult());
        pipeline.add(RetryConditions.hasExceptionCause());
    }
}
