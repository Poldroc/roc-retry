package com.poldroc.retry.annotation.annotation;

import com.poldroc.retry.annotation.annotation.metadata.RetryAble;
import com.poldroc.retry.annotation.handler.impl.DefaultRetryAbleHandler;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.core.core.retry.DefaultRetry;
import com.poldroc.retry.core.support.condition.ExceptionCauseRetryCondition;
import com.poldroc.retry.core.support.listen.NoRetryListen;
import com.poldroc.retry.core.support.recover.NoRecover;

import java.lang.annotation.*;

/**
 * 重试注解
 * 1. 实际需要，只允许放在方法上。
 * 2. 保持注解和接口的一致性 {@link com.poldroc.retry.api.core.Retry} 接口
 *
 * @author Poldroc
 * @date 2024/7/17
 */

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RetryAble(DefaultRetryAbleHandler.class)
public @interface Retry {

    /**
     * 重试类实现
     *
     * @return 重试
     */
    Class<? extends com.poldroc.retry.api.core.Retry> retry() default DefaultRetry.class;

    /**
     * 最大尝试次数
     * 1. 默认为3 包含方法第一次正常执行的次数
     *
     * @return 次数
     */
    int maxAttempt() default 3;

    /**
     * 重试触发的场景
     * 1. 默认为异常触发
     *
     * @return 重试触发的场景
     */
    Class<? extends RetryCondition> condition() default ExceptionCauseRetryCondition.class;

    /**
     * 监听器
     * 1. 默认不进行监听
     *
     * @return 监听器
     */
    Class<? extends RetryListen> listen() default NoRetryListen.class;

    /**
     * 恢复操作
     * 1. 默认不进行任何恢复操作
     *
     * @return 恢复操作对应的类
     */
    Class<? extends Recover> recover() default NoRecover.class;

    /**
     * 重试等待策略
     * 1. 支持指定多个，如果不指定，则不进行任何等待，
     *
     * @return 重试等待策略
     */
    RetryWait[] waits() default {};
}
