package com.poldroc.retry.api.support.recover;

import com.poldroc.retry.api.model.RetryAttempt;

/**
 * 恢复现场接口
 * 1. 只会在所有的尝试都执行完成之后才会执行。
 * 2. 触发了重试，且所有的重试都完成了，但结果依然是失败。
 * 3. 根据实际使用，一次失败对应的 recover 应该是唯一的。
 * 注意：实现类应该有无参构造函数
 * @author Poldroc
 * @date 2024/7/11
 *
 */
public interface Recover {

    /**
     * 执行恢复
     * @param retryAttempt 重试信息
     * @param <R> 泛型
     */
    <R> void recover(final RetryAttempt<R> retryAttempt);

}
