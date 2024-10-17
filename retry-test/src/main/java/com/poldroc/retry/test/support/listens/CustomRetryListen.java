package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.core.support.listen.AbstractRetryListenInit;

import java.util.LinkedList;

public class CustomRetryListen extends AbstractRetryListenInit {
    @Override
    protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
        // 自定义初始化逻辑，比如根据重试次数决定是否添加某个监听器
        if (attempt.attempt() == 2) {
            pipeline.add(new LogRetryListen());
        }
        pipeline.add(new StatRetryListen());
    }

    private class LogRetryListen implements RetryListen {

        @Override
        public <R> void listen(RetryAttempt<R> attempt) {
            System.out.println("LogRetryListen: " + attempt);
        }
    }

    private class StatRetryListen implements RetryListen {
        @Override
        public <R> void listen(RetryAttempt<R> attempt) {
            System.out.println("StatRetryListen");
        }
    }
}
