package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.core.support.listen.AbstractRetryListenInit;

import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public class MultipleCountingListen extends AbstractRetryListenInit {

    public final AtomicInteger listener1Count = new AtomicInteger(0);
    public final AtomicInteger listener2Count = new AtomicInteger(0);

    @Override
    protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
        pipeline.add(new ListenerOne());
        pipeline.add(new ListenerTwo());
    }

    public class ListenerOne implements RetryListen {
        @Override
        public <R> void listen(RetryAttempt<R> attempt) {
            listener1Count.incrementAndGet();
            System.out.println("ListenerOne has been called " + listener1Count.get() + " times.");
        }
    }
    public class ListenerTwo implements RetryListen {
        @Override
        public <R> void listen(RetryAttempt<R> attempt) {
            listener2Count.incrementAndGet();
            System.out.println("ListenerTwo has been called " + listener2Count.get() + " times.");
        }
    }
}
