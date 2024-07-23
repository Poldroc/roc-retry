package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;

public class MyListen1 implements RetryListen {
    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        System.out.println("MyListen1 listen attempt :" + attempt.attempt());
    }
}
