package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;

import java.util.Arrays;
import java.util.Collections;

public class MyListen3 implements RetryListen {
    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        System.out.println("MyListen3 listen history:" + Arrays.toString(attempt.history().toArray()));
    }
}
