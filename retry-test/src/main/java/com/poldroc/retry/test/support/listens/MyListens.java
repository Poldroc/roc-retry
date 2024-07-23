package com.poldroc.retry.test.support.listens;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.core.support.listen.RetryListens;

public class MyListens implements RetryListen {

    @Override
    public <R> void listen(RetryAttempt<R> attempt) {
        RetryListens.listens(new MyListen1(), new MyListen2(), new MyListen3()).listen(attempt);
    }
}
