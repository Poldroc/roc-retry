package com.poldroc.retry.api.support.listen;

import com.poldroc.retry.api.model.RetryAttempt;
/**
 * 重试监听接口
 * @author Poldroc
 *  
 */

public interface RetryListen {

    /**
     * 执行重试监听，每次重试执行的最后触发监听器
     * @param attempt 重试
     * @param <R> 泛型
     */
    <R> void listen(final RetryAttempt<R> attempt);

}
