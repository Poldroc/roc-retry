package com.poldroc.retry.core.support.listen;

import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;
/**
 * 不进行任何监听动作
 * @author Poldroc
 * @since 2024/7/12
 */

@ThreadSafe
public class NoRetryListen implements RetryListen {

    /**
     * 获取单例
     * @return 单例
     */
    public static RetryListen getInstance() {
        return InstanceFactory.getInstance().singleton(NoRetryListen.class);
    }

    @Override
    public <R> void listen(RetryAttempt<R> attempt) {

    }

}
