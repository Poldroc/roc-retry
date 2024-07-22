package com.poldroc.retry.annotation.handler.method;

import com.poldroc.retry.annotation.annotation.metadata.RetryAble;
import com.poldroc.retry.annotation.handler.RetryAbleHandler;
import com.poldroc.retry.annotation.model.RetryAbleBean;
import com.poldroc.retry.annotation.proxy.IMethodHandler;
import com.poldroc.retry.api.context.RetryContext;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;
import com.poldroc.retry.core.core.Retryer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * 默认的重试方法实现
 *
 * @author Poldroc
 * @date 2024/7/14
 */
@ThreadSafe
public class RetryMethodHandler implements IMethodHandler {
    @Override
    public Object handle(Object obj, Method method, Object[] args) throws Throwable {
        // 1. 判断注解信息
        Optional<RetryAbleBean> retryAnnotationOpt = findRetryAnnotation(method, args);
        // 没有重试注解
        if (!retryAnnotationOpt.isPresent()) {
            return method.invoke(obj, args);
        }
        // 2. 包含注解才进行处理
        RetryAbleBean retryAbleBean = retryAnnotationOpt.get();
        Callable callable = buildCallable(obj, method, args);
        RetryAbleHandler retryAbleHandler = InstanceFactory.getInstance().threadSafe(retryAbleBean.retryAble().value());

        // 3. 构建执行上下文
        RetryContext retryContext = retryAbleHandler.build(retryAbleBean.annotation(), callable);
        return Retryer.newInstance().retryCall(retryContext);
    }


    /**
     * 重试调用
     * @param retryAbleBean 重试调用对象
     * @param callable 待重试方法
     * @return 执行结果
     */
    public Object retryCall(RetryAbleBean retryAbleBean,Callable callable) {
        RetryAbleHandler retryAbleHandler = InstanceFactory.getInstance().threadSafe(retryAbleBean.retryAble().value());
        RetryContext retryContext = retryAbleHandler.build(retryAbleBean.annotation(), callable);
        retryContext.params(retryAbleBean.args());
        return Retryer.newInstance().retryCall(retryContext);
    }

    /**
     * 查找重试注解
     *
     * @param method 方法
     * @param args   参数
     * @return 重试注解
     */
    public Optional<RetryAbleBean> findRetryAnnotation(Method method,
                                                       Object[] args) {
        Annotation[] annotations = method.getAnnotations();
        if (annotations == null || annotations.length == 0) {
            return Optional.empty();
        }
        for (Annotation annotation : annotations) {
            RetryAble retryAble = annotation.annotationType().getAnnotation(RetryAble.class);
            if (retryAble != null) {
                RetryAbleBean bean = new RetryAbleBean();
                bean.retryAble(retryAble)
                        .annotation(annotation)
                        .args(args);
                return Optional.of(bean);
            }
        }
        return Optional.empty();
    }

    /**
     * 构建 callable
     *
     * @param proxy  代理对象
     * @param method 方法
     * @param args   参数
     * @return callable
     */
    private Callable buildCallable(final Object proxy, final Method method, final Object[] args) {
        return () -> method.invoke(proxy, args);
    }
}
