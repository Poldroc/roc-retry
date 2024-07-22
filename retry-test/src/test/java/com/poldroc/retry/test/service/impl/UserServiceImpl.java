package com.poldroc.retry.test.service.impl;


import com.poldroc.retry.annotation.annotation.Retry;
import com.poldroc.retry.test.service.UserService;

public class UserServiceImpl implements UserService {

    @Retry(maxAttempt = 5)
    @Override
    public void queryUser(long id) {
        System.out.println("查询用户...");
        throw new RuntimeException();
    }

}
