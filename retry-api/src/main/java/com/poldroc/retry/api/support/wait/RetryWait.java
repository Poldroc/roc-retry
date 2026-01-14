package com.poldroc.retry.api.support.wait;

import com.poldroc.retry.api.context.RetryWaitContext;
import com.poldroc.retry.api.model.WaitTime;

import java.lang.annotation.Annotation;

/**
 * 重试等待策略
 * 1. 所有的实现必须要有无参构造器，因为会基于反射处理类信息(newInstance)。
 * 2. 尽可能的保证为线程安全的，比如 stateless。
 * @author Poldroc
 *  
 */

public interface RetryWait{

    /**
     * 计算等待时间
     * @param retryWaitContext 上下文信息
     * @return 等待时间的结果信息
     */
    WaitTime waitTime(final RetryWaitContext retryWaitContext);

}
