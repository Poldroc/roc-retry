package com.poldroc.retry.api.support.block;

import com.poldroc.retry.api.model.WaitTime;
/**
 * 阻塞的方式
 * @author Poldroc
 * @since 2024/7/11
 */

public interface RetryBlock {

    /**
     * 阻塞方式
     * @param waitTime 等待时间
     */
    void block(final WaitTime waitTime);

}
