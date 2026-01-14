package com.poldroc.retry.test.support.recovers;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.recover.Recover;

import java.util.concurrent.atomic.AtomicInteger;

public class CountingRecover implements Recover {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public <R> void recover(RetryAttempt<R> retryAttempt) {
        counter.incrementAndGet();
        System.out.println("CountingRecover has been called " + counter.get() + " times.");
    }
}
