package com.poldroc.retry.core.support.recover;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;

/**
 * 不指定任何动作
 * @author binbin.hou
 * @since 0.0.1
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
