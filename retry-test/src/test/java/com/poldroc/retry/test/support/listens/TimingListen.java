package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.core.support.listen.AbstractRetryListenInit;

import java.util.LinkedList;


public class TimingListen extends AbstractRetryListenInit {

    @Override
    protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
        pipeline.add(new Listener());
    }

    private static class Listener implements RetryListen {
        @Override
        public <R> void listen(RetryAttempt<R> attempt) {
            // 每次重试时，打印出当前是第几次重试以及从开始到现在的耗时
            System.out.println("TimingListen, task has bend called " + attempt.attempt() + " times. Elapsed time: " + attempt.time().costTimeInMills() + " ms.");
        }
    }
}
