package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;

public class MyListen2 implements RetryListen {
    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        System.out.println("MyListen2 listen time:" + attempt.time().costTimeInMills() + "ms");
    }
}
