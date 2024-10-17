package com.poldroc.retry.test.service.impl;


import com.poldroc.retry.annotation.annotation.Retry;

import com.poldroc.retry.annotation.annotation.RetryWait;
import com.poldroc.retry.core.support.wait.FixedRetryWait;
import com.poldroc.retry.test.service.SpringRecoverService;
import com.poldroc.retry.test.support.listens.CustomRetryListen;
import com.poldroc.retry.test.support.recover.MyRecover;
import org.springframework.stereotype.Service;

@Service
public class SpringRecoverServiceImpl implements SpringRecoverService {

    @Override
    @Retry(recover = MyRecover.class,listen = CustomRetryListen.class,waits = @RetryWait(value = 2000, retryWait = FixedRetryWait.class))
    public String query(String email) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("spring service query..." + email);
        throw new RuntimeException();
    }

}
