package com.poldroc.retry.annotation.proxy.dynamic;

import com.poldroc.retry.annotation.handler.method.RetryMethodHandler;
import com.poldroc.retry.annotation.proxy.IProxy;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.impl.InstanceFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 动态代理
 *
 * @author Poldroc
 * @date 2024/7/14
 */

@ThreadSafe
public class DynamicProxy implements InvocationHandler, IProxy {

    /**
     * 目标对象
     */
    private final Object target;

    public DynamicProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object proxy() {
        InvocationHandler handler = new DynamicProxy(target);
        return Proxy.newProxyInstance(handler.getClass().getClassLoader(),
                target.getClass().getInterfaces(), handler);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            RetryMethodHandler retryMethodHandler = InstanceFactory.getInstance().threadSafe(RetryMethodHandler.class);
            return retryMethodHandler.handle(target, method, args);
        } catch (InvocationTargetException ex) {
            // 程序内部没有处理的异常
            throw ex.getTargetException();
        } catch (Throwable throwable) {
            throw throwable;
        }
    }
}
