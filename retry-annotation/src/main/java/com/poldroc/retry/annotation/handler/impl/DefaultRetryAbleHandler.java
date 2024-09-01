package com.poldroc.retry.annotation.handler.impl;

import com.poldroc.retry.annotation.annotation.Retry;
import com.poldroc.retry.annotation.annotation.RetryWait;
import com.poldroc.retry.annotation.handler.RetryAbleHandler;
import com.poldroc.retry.api.context.RetryContext;
import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.Instance;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;
import com.poldroc.retry.core.core.RetryWaiter;
import com.poldroc.retry.core.core.Retryer;
import com.poldroc.retry.core.support.wait.NoRetryWait;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
/**
 * 默认的重试处理器
 * @author Poldroc
 * @since 2024/7/18
 */

@ThreadSafe
public class DefaultRetryAbleHandler<R> implements RetryAbleHandler<Retry, R> {

    @Override
    public RetryContext<R> build(Retry annotation, Callable<R> callable) {
        Instance instance = InstanceFactory.getInstance();
        return Retryer.<R>newInstance()
                .callable(callable)
                .retry(instance.threadSafe(annotation.retry()))
                .condition(instance.threadSafe(annotation.condition()))
                .maxAttempt(annotation.maxAttempt())
                .recover(instance.threadSafe(annotation.recover()))
                .listen(instance.threadSafe(annotation.listen()))
                .retryWaitContext(buildRetryWaitContext(annotation))
                .context();
    }

    /**
     * 构建重试等待上下文
     * @param retry 重试信息
     * @return 上下文列表
     */
    private List<RetryWaitContext<R>> buildRetryWaitContext(Retry retry) {
        if (retry == null) {
            return Collections.singletonList(RetryWaiter.<R>retryWait(NoRetryWait.class).context());
        }
        RetryWait[] waits = retry.waits();
        if (waits == null || waits.length == 0) {
            return Collections.singletonList(RetryWaiter.<R>retryWait(NoRetryWait.class).context());
        }
        List<RetryWaitContext<R>>  retryWaitContexts = new ArrayList<>();
        DefaultRetryWaitAbleHandler defaultRetryWaitAbleHandler = InstanceFactory.getInstance().threadSafe(DefaultRetryWaitAbleHandler.class);
        for (RetryWait wait : waits) {
            retryWaitContexts.add(defaultRetryWaitAbleHandler.build(wait));
        }
        return retryWaitContexts;
    }
}

