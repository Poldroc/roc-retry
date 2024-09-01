package com.poldroc.retry.core.support.recover;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;

/**
 * 没有任何恢复操作
 * @author Poldroc
 * @since 2024/9/1
 */

@ThreadSafe
public class NoRecover implements Recover {

    /**
     * 获取一个单例示例
     * @return 单例示例
     */
    public static Recover getInstance() {
        return InstanceFactory.getInstance().singleton(NoRecover.class);
    }

    @Override
    public <R> void recover(RetryAttempt<R> retryAttempt) {

    }
}
