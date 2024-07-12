package com.poldroc.retry.core.support.condition;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.common.support.impl.InstanceFactory;

import java.util.LinkedList;

/**
 * 重试条件工具类
 *
 * @author Poldroc
 * @date 2024/7/12
 */

public class RetryConditions {
    private RetryConditions() {
    }

    /**
     * 结果为空
     *
     * @param <R> 单例
     * @return 结果为空
     */
    public static <R> RetryCondition<R> isNullResult() {
        return InstanceFactory.getInstance().singleton(NullResultRetryCondition.class);
    }

    /**
     * 结果不为空
     *
     * @param <R> 单例
     * @return 结果为空
     */
    public static <R> RetryCondition<R> isNotNullResult() {
        return InstanceFactory.getInstance().singleton(NotNullResultRetryCondition.class);
    }


    /**
     * 结果等于预期值
     * 注意：null 值不等于任何值。
     *
     * @param excepted 预期值
     * @param <R>      单例
     * @return 结果为空
     */
    public static <R> RetryCondition<R> isEqualsResult(final R excepted) {
        return new AbstractResultRetryCondition<R>() {
            @Override
            protected boolean resultCondition(R result) {
                if (result == null) {
                    return false;
                }
                return result.equals(excepted);
            }
        };
    }


    /**
     * 结果不等于预期值
     *
     * @param excepted 预期值
     * @param <R>      单例
     * @return 结果为空
     */
    public static <R> RetryCondition<R> isNotEqualsResult(final R excepted) {
        return new AbstractResultRetryCondition<R>() {
            @Override
            protected boolean resultCondition(R result) {
                if (result == null) {
                    return true;
                }
                return !result.equals(excepted);
            }
        };
    }

    /**
     * 程序执行过程中遇到异常
     *
     * @return 重试条件
     */
    public static RetryCondition hasExceptionCause() {
        return InstanceFactory.getInstance().singleton(ExceptionCauseRetryCondition.class);
    }

    /**
     * 是预期的异常类型
     *
     * @param exceptionClass 异常类型
     * @return 重试条件
     */
    public static RetryCondition isExceptionCause(final Class<? extends Throwable> exceptionClass) {
        return new AbstractCauseRetryCondition() {
            @Override
            protected boolean causeCondition(Throwable throwable) {
                return exceptionClass.isAssignableFrom(throwable.getClass());
            }
        };
    }

    /**
     * 多条件整合
     */
    public static RetryCondition conditions(final RetryCondition... conditions) {
        if (conditions == null || conditions.length == 0) {
            return AlwaysFalseRetryCondition.getInstance();
        }
        return new AbstractRetryConditionInit() {
            @Override
            protected void init(LinkedList<RetryCondition> pipeline, RetryAttempt retryAttempt) {
                for (RetryCondition condition : conditions) {
                    pipeline.addLast(condition);
                }
            }
        };
    }


}
