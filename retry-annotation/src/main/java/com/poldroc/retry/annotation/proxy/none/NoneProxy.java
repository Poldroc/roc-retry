package com.poldroc.retry.annotation.proxy.none;

import com.poldroc.retry.annotation.proxy.IProxy;
import com.poldroc.retry.common.annotation.ThreadSafe;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

@ThreadSafe
public class NoneProxy implements InvocationHandler, IProxy {
    private final Object target;

    public NoneProxy(Object target) {
        this.target = target;
    }

    public Object proxy() {
        return this.target;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(proxy, args);
    }
}
