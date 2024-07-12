package com.poldroc.retry.api.support.listen;

import com.poldroc.retry.api.model.RetryAttempt;
/**
 * 重试监听接口
 * @author Poldroc
 * @date 2024/7/11
 */

public interface RetryListen {

    /**
     * 执行重试监听
     * @param attempt 重试
     * @param <R> 泛型
     */
    <R> void listen(final RetryAttempt<R> attempt);

}
