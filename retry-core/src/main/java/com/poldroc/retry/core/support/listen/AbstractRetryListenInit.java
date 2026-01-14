package com.poldroc.retry.core.support.listen;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.common.annotation.ThreadSafe;

import java.util.LinkedList;

/**
 * 监听器初始化
 *
 * @author Poldroc
 *  
 */

@ThreadSafe
public abstract class AbstractRetryListenInit implements RetryListen {
    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        LinkedList<RetryListen> listens = new LinkedList<>();
        this.init(listens, attempt);
        // 执行
        for (RetryListen listen : listens) {
            listen.listen(attempt);
        }
    }

    protected abstract void init(final LinkedList<RetryListen> pipeline, final RetryAttempt attempt);
}
