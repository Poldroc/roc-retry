package com.poldroc.retry.core.core.retry;

import com.poldroc.retry.api.context.RetryContext;
import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.core.Retry;
import com.poldroc.retry.api.exception.RetryException;
import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.api.support.block.RetryBlock;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.api.support.stop.RetryStop;
import com.poldroc.retry.api.support.wait.RetryWait;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;
import com.poldroc.retry.core.context.DefaultRetryWaitContext;
import com.poldroc.retry.core.model.DefaultAttemptTime;
import com.poldroc.retry.core.model.DefaultRetryAttempt;
import com.poldroc.retry.core.model.DefaultWaitTime;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * 默认的重试实现
 *
 * @author Poldroc
 * @date 2024/7/11
 */

@ThreadSafe
public class DefaultRetry<R> implements Retry<R> {

    /**
     * 获取单例
     *
     * @return 单例
     */
    public static DefaultRetry getInstance() {
        return InstanceFactory.getInstance().singleton(DefaultRetry.class);
    }

    @Override
    public R retryCall(RetryContext<R> context) {
        List<RetryAttempt<R>> history = new ArrayList<>();
        int attempts = 1;
        final Callable<R> callable = context.callable();
        RetryAttempt<R> retryAttempt = execute(callable, attempts, history, context);

        final List<RetryWaitContext<R>> waitContextList = context.waitContext();
        final RetryCondition retryCondition = context.condition();
        final RetryStop retryStop = context.stop();
        final RetryBlock retryBlock = context.block();
        final RetryListen retryListen = context.listen();

        // 触发执行的 condition 并且 不触发 stop 策略 就进行重试
        while (retryCondition.condition(retryAttempt) && !retryStop.stop(retryAttempt)) {
            // 线程阻塞
            WaitTime waitTime = calcWaitTime(waitContextList, retryAttempt);
            retryBlock.block(waitTime);
            // 每一次执行会更新 executeResult
            attempts++;
            history.add(retryAttempt);
            retryAttempt = this.execute(callable, attempts, history, context);

            // 触发监听器
            retryListen.listen(retryAttempt);
        }

        // 仍然满足重试条件，但是满足重试停止条件
        if (retryCondition.condition(retryAttempt) && retryStop.stop(retryAttempt)) {
            // 触发恢复策略
            final Recover recover = context.recover();
            recover.recover(retryAttempt);
        }

        // 最后一次还是有异常，直接抛出异常
        final Throwable throwable = retryAttempt.cause();
        if (throwable != null) {
            // 1. 运行时异常，则直接抛出
            // 2. 非运行时异常，则包装成为 RetryException
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException) throwable;
            }
            throw new RetryException(retryAttempt.cause());
        }
        // 返回最后一次尝试的结果
        return retryAttempt.result();
    }

    /**
     * 构建等待时间
     *
     * @param waitContextList 等待上下文列表
     * @param retryAttempt    重试信息
     * @return 等待时间毫秒
     */
    private WaitTime calcWaitTime(final List<RetryWaitContext<R>> waitContextList,
                                  final RetryAttempt<R> retryAttempt) {
        long totalTimeMills = 0;
        for (RetryWaitContext context : waitContextList) {
            RetryWait retryWait = (RetryWait) InstanceFactory.getInstance().threadSafe(context.retryWait());
            final RetryWaitContext retryWaitContext = buildRetryWaitContext(context, retryAttempt);
            WaitTime waitTime = retryWait.waitTime(retryWaitContext);
            totalTimeMills += TimeUnit.MILLISECONDS.convert(waitTime.time(), waitTime.unit());
        }
        return new DefaultWaitTime(totalTimeMills);
    }

    private RetryWaitContext buildRetryWaitContext(RetryWaitContext waitContext, RetryAttempt<R> retryAttempt) {
        DefaultRetryWaitContext<R> context = (DefaultRetryWaitContext<R>) waitContext;
        return context.attempt(retryAttempt.attempt())
                .cause(retryAttempt.cause())
                .result(retryAttempt.result())
                .time(retryAttempt.time())
                .history(retryAttempt.history())
                .params(retryAttempt.params());

    }

    private RetryAttempt<R> execute(final Callable<R> callable,
                                    final int attempts,
                                    final List<RetryAttempt<R>> history,
                                    final RetryContext<R> context) {

        Date startTime = new Date();
        Throwable throwable = null;
        R result = null;
        try {
            result = callable.call();
        } catch (Exception e) {
            throwable = getActualThrowable(e);
        }
        Date endTime = new Date();
        DefaultAttemptTime attemptTime = new DefaultAttemptTime()
                .startTime(startTime)
                .endTime(endTime)
                .costTimeInMills(endTime.getTime() - startTime.getTime());
        return new DefaultRetryAttempt<R>()
                .result(result)
                .attempt(attempts)
                .cause(throwable)
                .time(attemptTime)
                // 设置请求入参，主要用于回调等使用。
                .params(context.params())
                .history(history);
    }

    /**
     * 获取实际的异常信息
     *
     * @param throwable 异常信息
     * @return 实际的异常信息
     */
    private Throwable getActualThrowable(Throwable throwable) {
        if (InvocationTargetException.class.equals(throwable.getClass())) {
            InvocationTargetException exception = (InvocationTargetException) throwable;
            return exception.getTargetException();
        } else {
            return throwable;
        }
    }
}
