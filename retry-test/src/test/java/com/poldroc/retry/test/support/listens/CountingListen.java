package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.core.support.listen.AbstractRetryListenInit;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class CountingListen extends AbstractRetryListenInit {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
        pipeline.add(new Listener());
    }

    private class Listener implements RetryListen {
        @Override
        public <R> void listen(RetryAttempt<R> attempt) {
            counter.incrementAndGet();
            System.out.println("CountingRetryListen has been called " + counter.get() + " times.");
        }
    }
}
