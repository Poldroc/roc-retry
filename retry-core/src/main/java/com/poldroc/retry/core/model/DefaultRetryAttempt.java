package com.poldroc.retry.core.model;

import com.poldroc.retry.api.model.AttemptTime;
import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.common.annotation.NotThreadSafe;

import java.util.Arrays;
import java.util.List;

/**
 * 默认重试信息
 *
 * @author Poldroc
 * @date 2024/7/11
 */

@NotThreadSafe
public class DefaultRetryAttempt<R> implements RetryAttempt<R> {

    /**
     * 执行结果
     */
    private R result;

    /**
     * 尝试次数
     */
    private int attempt;

    /**
     * 异常信息
     */
    private Throwable cause;

    /**
     * 消耗时间
     */
    private AttemptTime time;

    /**
     * 历史信息
     */
    private List<RetryAttempt<R>> history;

    /**
     * 请求参数
     *
     * @since 0.1.0
     */
    private Object[] params;

    @Override
    public R result() {
        return result;
    }

    public DefaultRetryAttempt<R> result(R result) {
        this.result = result;
        return this;
    }

    @Override
    public int attempt() {
        return attempt;
    }

    public DefaultRetryAttempt<R> attempt(int attempt) {
        this.attempt = attempt;
        return this;
    }

    @Override
    public Throwable cause() {
        return cause;
    }

    public DefaultRetryAttempt<R> cause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public AttemptTime time() {
        return time;
    }

    public DefaultRetryAttempt<R> time(AttemptTime time) {
        this.time = time;
        return this;
    }

    @Override
    public List<RetryAttempt<R>> history() {
        return history;
    }

    public DefaultRetryAttempt<R> history(List<RetryAttempt<R>> history) {
        this.history = history;
        return this;
    }

    @Override
    public Object[] params() {
        return params;
    }

    public DefaultRetryAttempt<R> params(Object[] params) {
        this.params = params;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultRetryAttempt{" +
                "result=" + result +
                ", attempt=" + attempt +
                ", cause=" + cause +
                ", time=" + time +
                ", params=" + Arrays.toString(params) +
                '}';
    }

}
