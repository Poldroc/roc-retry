package com.poldroc.retry.spring.aop;

import com.poldroc.retry.annotation.handler.method.RetryMethodHandler;
import com.poldroc.retry.annotation.model.RetryAbleBean;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * 重试 aop
 *
 * @author Poldroc
 * @date 2024/7/22
 */

@Aspect
@Component
public class RetryAop {


    @Pointcut("@annotation(com.poldroc.retry.annotation.annotation.Retry)")
    public void myPointcut() {
    }


    @Around("myPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        // 得到方法签名
        Signature signature = point.getSignature();
        // 得到方法
        Method method = ((MethodSignature) signature).getMethod();
        RetryMethodHandler retryMethodHandler = InstanceFactory.getInstance().singleton(RetryMethodHandler.class);
        Object[] args = point.getArgs();
        Optional<RetryAbleBean> retryAnnotation = retryMethodHandler.findRetryAnnotation(method, args);
        if (!retryAnnotation.isPresent()) {
            return point.proceed();
        }
        Callable callable = buildCallable(point);
        RetryAbleBean retryAbleBean = retryAnnotation.get();
        return retryMethodHandler.retryCall(retryAbleBean, callable);
    }

    private Callable buildCallable(ProceedingJoinPoint point) {
        return () -> {
            try {
                return point.proceed();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
    }
}
