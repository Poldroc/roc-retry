package com.poldroc.retry.test.core;

import com.poldroc.retry.annotation.core.RetryTemplate;
import com.poldroc.retry.test.service.impl.UserServiceImpl;
import org.junit.Test;

public class RetryTemplateTest {


    @Test(expected = RuntimeException.class)
    public void templateTest() {
        UserServiceImpl userService = RetryTemplate.getProxyObject(new UserServiceImpl());
        userService.queryUser(1);
    }
}
