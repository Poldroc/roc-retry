package com.poldroc.retry.api.model;

import java.util.List;

/**
 * 重试信息接口
 * @author Poldroc
 *  
 */

public interface RetryAttempt<R> {

    /**
     * 获取方法执行的结果
     * @return 方法执行的结果
     */
    R result();

    /**
     * 获取重试次数
     * @return 重试次数
     */
    int attempt();

    /**
     * 获取异常信息
     * @return 异常信息
     */
    Throwable cause();

    /**
     * 获取消耗时间
     * @return 消耗时间
     */
    AttemptTime time();

    /**
     * 获取重试历史信息
     * @return 重试历史信息
     */
    List<RetryAttempt<R>> history();

    /**
     * 获取请求参数
     * @return 请求参数
     */
    Object[] params();
}
