package com.poldroc.retry.api.support.stop;

import com.poldroc.retry.api.model.RetryAttempt;
/**
 * 结束的条件
 * @author Poldroc
 *  
 */

public interface RetryStop {

    /**
     * 停止执行重试
     * @param attempt 执行信息
     * @return 是否停止
     */
    boolean stop(final RetryAttempt attempt);

}
