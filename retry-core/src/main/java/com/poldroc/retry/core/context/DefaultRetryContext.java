package com.poldroc.retry.core.context;

import com.poldroc.retry.api.context.RetryContext;
import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.core.Retry;
import com.poldroc.retry.api.support.block.RetryBlock;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.api.support.stop.RetryStop;
import com.poldroc.retry.common.annotation.NotThreadSafe;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * 默认重试执行上下文
 *
 * @author Poldroc
 * @date 2024/7/11
 */

@NotThreadSafe
public class DefaultRetryContext<R> implements RetryContext<R> {

    /**
     * 重试实现类
     */
    private Retry<R> retry;

    /**
     * 重试生效条件
     */
    private RetryCondition condition;

    /**
     * 重试等待上下文
     */
    private List<RetryWaitContext<R>> waitContext;

    /**
     * 阻塞实现
     */
    private RetryBlock block;

    /**
     * 停止策略
     */
    private RetryStop stop;

    /**
     * 可执行的方法
     */
    private Callable<R> callable;

    /**
     * 监听器
     */
    private RetryListen listen;

    /**
     * 恢复策略
     */
    private Recover recover;

    /**
     * 请求参数信息
     */
    private Object[] params;


    @Override
    public Retry<R> retry() {
        return retry;
    }

    public DefaultRetryContext<R> retry(Retry<R> retry) {
        this.retry = retry;
        return this;
    }

    @Override
    public RetryCondition condition() {
        return condition;
    }

    public DefaultRetryContext<R> condition(RetryCondition condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public List<RetryWaitContext<R>> waitContext() {
        return waitContext;
    }

    public DefaultRetryContext<R> waitContext(List<RetryWaitContext<R>> waitContext) {
        this.waitContext = waitContext;
        return this;
    }

    @Override
    public RetryBlock block() {
        return block;
    }

    public DefaultRetryContext<R> block(RetryBlock block) {
        this.block = block;
        return this;
    }

    @Override
    public RetryStop stop() {
        return stop;
    }

    public DefaultRetryContext<R> stop(RetryStop stop) {
        this.stop = stop;
        return this;
    }

    @Override
    public Callable<R> callable() {
        return callable;
    }

    public DefaultRetryContext<R> retry(Callable<R> callable) {
        this.callable = callable;
        return this;
    }

    @Override
    public RetryListen listen() {
        return listen;
    }

    public DefaultRetryContext<R> listen(RetryListen listen) {
        this.listen = listen;
        return this;
    }

    @Override
    public Recover recover() {
        return recover;
    }

    public DefaultRetryContext<R> recover(Recover recover) {
        this.recover = recover;
        return this;
    }

    @Override
    public Object[] params() {
        return params;
    }

    @Override
    public DefaultRetryContext<R> params(Object[] params) {
        this.params = params;
        return this;
    }
}
