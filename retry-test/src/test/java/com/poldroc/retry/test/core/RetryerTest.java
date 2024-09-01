package com.poldroc.retry.test.core;

import com.poldroc.retry.core.core.RetryWaiter;
import com.poldroc.retry.core.core.Retryer;
import com.poldroc.retry.core.support.condition.RetryConditions;
import com.poldroc.retry.core.support.listen.RetryListens;
import com.poldroc.retry.core.support.recover.Recovers;
import com.poldroc.retry.core.support.wait.NoRetryWait;
import org.junit.Test;

import java.util.concurrent.Callable;

public class RetryerTest {

    /**
     * 不会触发重试
     */
    @Test
    public void commonTest() {
        Retryer.<String>newInstance()
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("called...");
                        return null;
                    }
                }).retryCall();
    }

    /**
     * 默认异常进行重试
     */
    @Test(expected = RuntimeException.class)
    public void helloTest() {
        Retryer.<String>newInstance()
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("called...");
                        throw new RuntimeException();
                    }
                }).retryCall();
    }

    /**
     * 默认配置测试
     */
    @Test(expected = RuntimeException.class)
    public void defaultConfigTest() {
        Retryer.<String>newInstance()
                .maxAttempt(4)
                .listen(RetryListens.noListen())
                .recover(Recovers.noRecover())
                .condition(RetryConditions.hasExceptionCause())
                .retryWaitContext(RetryWaiter.<String>retryWait(NoRetryWait.class).context())
                .callable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        System.out.println("called...");
                        throw new RuntimeException();
                    }
                }).retryCall();
    }

}
