package com.poldroc.retry.core.support.listen;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;

import java.util.LinkedList;

/**
 * 监听器工具类
 *
 * @author Poldroc
 *  
 */

public class RetryListens {

    private RetryListens() {
    }

    /**
     * 不进行任何监听动作
     *
     * @return 监听器
     */
    public static RetryListen noListen() {
        return NoRetryListen.getInstance();
    }

    /**
     * 指定多个监听器
     *
     * @param retryListens 多个监听器信息
     * @return 监听器
     */
    public static RetryListen listens(final RetryListen... retryListens) {
        if (null == retryListens || retryListens.length == 0) {
            return noListen();
        }
        return new AbstractRetryListenInit() {
            @Override
            protected void init(LinkedList<RetryListen> pipeline, RetryAttempt attempt) {
                for (RetryListen retryListen : retryListens) {
                    pipeline.addLast(retryListen);
                }
            }
        };
    }
}
