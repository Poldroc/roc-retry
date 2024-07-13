package com.poldroc.retry.core.core;

import com.poldroc.retry.api.support.wait.RetryWait;
import com.poldroc.retry.common.annotation.NotThreadSafe;
import com.poldroc.retry.core.constant.RetryWaitConst;
import com.poldroc.retry.core.support.wait.ExponentialRetryWait;
import com.poldroc.retry.core.support.wait.IncreaseRetryWait;
import com.poldroc.retry.core.support.wait.NoRetryWait;

/**
 * 重试等待类构造器
 *
 * @author Poldroc
 * @date 2024/7/11
 */

@NotThreadSafe
public class RetryWaiter<R> {

    /**
     * 重试等待类的类型
     */
    private Class<? extends RetryWait> retryWait = NoRetryWait.class;

    /**
     * 默认的等待时间
     */
    private long value = RetryWaitConst.DEFAULT_VALUE_MILLS;

    /**
     * 最小值
     */
    private long min = RetryWaitConst.DEFAULT_MIN_MILLS;

    /**
     * 最大值
     */
    private long max = RetryWaitConst.DEFAULT_MAX_MILLS;

    /**
     * 变化因子
     * <p>
     * 1. 如果是 {@link com.poldroc.retry.core.support.wait.ExponentialRetryWait} 则为 {@link com.poldroc.retry.core.constant.RetryWaitConst#MULTIPLY_FACTOR}
     * <p>
     * 2. 如果是 {@link com.poldroc.retry.core.support.wait.IncreaseRetryWait} 则为 {@link com.poldroc.retry.core.constant.RetryWaitConst#INCREASE_MILLS_FACTOR}
     */
    private double factor = Double.MIN_VALUE;

    /**
     * 构造器私有化
     */
    private RetryWaiter() {
    }

    /**
     * 设置重试等待的对象类型
     *
     * @param retryWait 重试等待类
     * @param <R>       泛型
     * @return 重试等待类
     */
    public static <R> RetryWaiter<R> retryWait(Class<? extends RetryWait> retryWait) {
        RetryWaiter<R> retryWaiter = new RetryWaiter<>();
        retryWaiter.retryWait = retryWait;
        if (IncreaseRetryWait.class.equals(retryWait)) {
            retryWaiter.factor(RetryWaitConst.INCREASE_MILLS_FACTOR);
        }
        if (ExponentialRetryWait.class.equals(retryWait)) {
            retryWaiter.factor(RetryWaitConst.MULTIPLY_FACTOR);
        }
        return retryWaiter;
    }

    public Class<? extends RetryWait> retryWait() {
        return retryWait;
    }

    public long value() {
        return value;
    }

    public RetryWaiter<R> value(long value) {
        this.value = value;
        return this;
    }

    public long min() {
        return min;
    }

    public RetryWaiter<R> min(long min) {
        this.min = min;
        return this;
    }

    public long max() {
        return max;
    }

    public RetryWaiter<R> max(long max) {
        this.max = max;
        return this;
    }

    public double factor() {
        return factor;
    }

    public RetryWaiter<R> factor(double factor) {
        this.factor = factor;
        return this;
    }

}
