package com.poldroc.retry.api.context;

import com.poldroc.retry.api.core.Retry;
import com.poldroc.retry.api.support.block.RetryBlock;
import com.poldroc.retry.api.support.condition.RetryCondition;
import com.poldroc.retry.api.support.listen.RetryListen;
import com.poldroc.retry.api.support.recover.Recover;
import com.poldroc.retry.api.support.stop.RetryStop;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * 重试执行上下文
 * 限定实现类的参数
 * @author Poldroc
 *  
 */

public interface RetryContext<R> {

    /**
     * 重试实现类
     * @return 重试
     */
    Retry<R> retry();

    /**
     * 重试生效条件
     * @return 生效条件
     */
    RetryCondition condition();

    /**
     * 重试等待上下文
     * @return 重试等待上下文
     */
    List<RetryWaitContext<R>> waitContext();

    /**
     * 阻塞方式
     * @return 阻塞方式
     */
    RetryBlock block();

    /**
     * 停止方式
     * @return 停止方式
     */
    RetryStop stop();

    /**
     * 可执行的方法
     * @return 方法
     */
    Callable<R> callable();

    /**
     * 监听信息列表
     * @return 监听信息列表
     */
    RetryListen listen();

    /**
     * 恢复方式
     * @return 恢复方式
     */
    Recover recover();

    /**
     * 请求参数
     * @return 请求参数
     */
    Object[] params();

    /**
     * 设置上下文的信息
     * @param params 参数
     * @return this
     */
    RetryContext<R> params(Object[] params);


}
