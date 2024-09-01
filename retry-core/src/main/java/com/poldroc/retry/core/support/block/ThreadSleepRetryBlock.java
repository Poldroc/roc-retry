package com.poldroc.retry.core.support.block;

import com.poldroc.retry.api.exception.RetryException;
import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.api.support.block.RetryBlock;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;

/**
 * 线程睡眠的阻塞方法
 * @author Poldroc
 * @since 2024/7/11
 */
@ThreadSafe
public class ThreadSleepRetryBlock implements RetryBlock {

    /**
     * 获取单例
     * @return 获取单例
     */
    public static RetryBlock getInstance() {
        return InstanceFactory.getInstance().singleton(ThreadSleepRetryBlock.class);
    }

    @Override
    public void block(WaitTime waitTime) {
        try {
            waitTime.unit().sleep(waitTime.time());
        } catch (InterruptedException e) {
            // 恢复状态
            Thread.currentThread().interrupt();
            throw new RetryException(e);
        }
    }
}
