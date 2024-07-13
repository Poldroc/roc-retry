package com.poldroc.retry.core.core;

import com.poldroc.retry.api.context.RetryContext;
import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.core.Retry;
import com.poldroc.retry.api.support.block.RetryBlock;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.api.support.stop.RetryStop;
import com.poldroc.retry.common.annotation.NotThreadSafe;
import com.poldroc.retry.common.util.ArgUtil;
import com.poldroc.retry.core.context.DefaultRetryContext;
import com.poldroc.retry.core.core.retry.DefaultRetry;
import com.poldroc.retry.core.support.block.ThreadSleepRetryBlock;
import com.poldroc.retry.core.support.condition.RetryConditions;
import com.poldroc.retry.core.support.listen.NoRetryListen;
import com.poldroc.retry.core.support.recover.NoRecover;
import com.poldroc.retry.core.support.stop.MaxAttemptRetryStop;
import com.poldroc.retry.core.support.wait.NoRetryWait;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 引导核心类
 *
 * @author Poldroc
 * @date 2024/7/13
 */
@NotThreadSafe
public class Retryer<R> implements Retry<R> {

    /**
     * 待执行的方法
     */
    private Callable<R> callable;

    /**
     * 重试实现类
     * 1. 不推荐用户自定义，但是暴露出来。
     */
    private Retry<R> retry = DefaultRetry.getInstance();

    /**
     * 执行重试的条件
     * 1. 默认在遇到异常的时候进行重试
     * 2. 支持多个条件，任意一个满足则满足。如果用户有更特殊的需求，应该自己定义。
     */
    private RetryCondition condition = RetryConditions.hasExceptionCause();

    /**
     * 阻塞的方式
     * 1. 默认采用线程沉睡的方式
     */
    private RetryBlock block = ThreadSleepRetryBlock.getInstance();

    /**
     * 重试停止的条件
     * 1. 默认最多重试 3 次
     */
    private RetryStop stop = new MaxAttemptRetryStop(3);

    /**
     * 监听器
     * 1. 默认不进行任何操作
     */
    private RetryListen listen = NoRetryListen.getInstance();

    /**
     * 恢复策略
     * 1. 默认不进行任何操作
     */
    private Recover recover = NoRecover.getInstance();

    /**
     * 重试等待上下文
     */
    private List<RetryWaitContext<R>> waitContexts = Collections.singletonList(RetryWaiter.<R>retryWait(NoRetryWait.class).context());


    /**
     * 创建一个对象实例
     *
     * @param <R> 泛型
     * @return 实例
     */
    public static <R> Retryer<R> newInstance() {
        return new Retryer<>();
    }

    /**
     * 1. 设置待处理的方法类
     * 2. 返回引导类 instance
     *
     * @param callable 待处理的方法
     * @return this
     */
    public Retryer<R> callable(final Callable<R> callable) {
        ArgUtil.notNull(callable, "callable");

        this.callable = callable;
        return this;
    }


    /**
     * 设置重试实现类
     *
     * @param retry 重试实现类
     * @return this
     */
    public Retryer<R> retry(Retry<R> retry) {
        this.retry = retry;
        return this;
    }

    /**
     * 重试生效条件
     *
     * @param condition 生效条件
     * @return this
     */
    public Retryer<R> condition(RetryCondition condition) {
        ArgUtil.notNull(condition, "condition");

        this.condition = condition;
        return this;
    }


    /**
     * 重试等待上下文
     *
     * @param retryWaitContexts 重试等待上下文数组
     * @return 重试等待上下文
     */
    public Retryer<R> retryWaitContext(RetryWaitContext<R>... retryWaitContexts) {
        ArgUtil.notEmpty(retryWaitContexts, "retryWaitContexts");
        this.waitContexts = Arrays.asList(retryWaitContexts);
        return this;
    }

    /**
     * 最大尝试次数
     *
     * @param maxAttempt 最大尝试次数
     * @return this
     */
    public Retryer<R> maxAttempt(final int maxAttempt) {
        ArgUtil.positive(maxAttempt, "maxAttempt");

        this.stop = new MaxAttemptRetryStop(maxAttempt);
        return this;
    }

    /**
     * 设置阻塞策略
     *
     * @param block 阻塞策略
     * @return this
     */
    private Retryer<R> block(RetryBlock block) {
        ArgUtil.notNull(block, "block");

        this.block = block;
        return this;
    }

    /**
     * 设置停止策略
     *
     * @param stop 停止策略
     * @return this
     */
    private Retryer<R> stop(RetryStop stop) {
        ArgUtil.notNull(stop, "stop");

        this.stop = stop;
        return this;
    }

    /**
     * 设置监听
     *
     * @param listen 监听
     * @return this
     */
    public Retryer<R> listen(RetryListen listen) {
        ArgUtil.notNull(listen, "listen");

        this.listen = listen;
        return this;
    }

    /**
     * 设置恢复策略
     *
     * @param recover 恢复策略
     * @return this
     */
    public Retryer<R> recover(Recover recover) {
        ArgUtil.notNull(recover, "recover");

        this.recover = recover;
        return this;
    }


    /**
     * 构建重试上下文
     *
     * @return 重试上下文
     */
    public RetryContext<R> context() {
        // 初始化
        return new DefaultRetryContext<R>()
                .callable(callable)
                .waitContext(waitContexts)
                .block(block)
                .stop(stop)
                .condition(condition)
                .listen(listen)
                .recover(recover)
                .retry(retry);
    }


    /**
     * 重试执行
     *
     * @return 执行结果
     */
    public R retryCall() {
        RetryContext<R> context = context();
        return context().retry().retryCall(context);
    }

    /**
     * 重试执行
     *
     * @param context 执行上下文
     * @return 执行结果
     */
    @Override
    public R retryCall(RetryContext<R> context) {
        return context().retry().retryCall(context);
    }
}
