package com.poldroc.retry.annotation.proxy.cglib;

import com.poldroc.retry.annotation.handler.method.RetryMethodHandler;
import com.poldroc.retry.annotation.proxy.IProxy;
import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.impl.InstanceFactory;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
/**
 * CGLIB 代理
 * @author Poldroc
 * @date 2024/7/14
 */

@ThreadSafe
public class CglibProxy implements IProxy, MethodInterceptor {
    /**
     * 被代理的对象
     */
    private final Object target;

    public CglibProxy(Object target) {
        this.target = target;
    }

    @Override
    public Object proxy() {
        Enhancer enhancer = new Enhancer();
        // 目标对象类
        enhancer.setSuperclass(target.getClass());
        // 设置回调
        enhancer.setCallback(this);
        // 通过字节码技术创建目标对象类的子类实例作为代理
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        try {
            RetryMethodHandler retryMethodHandler = InstanceFactory.getInstance().threadSafe(RetryMethodHandler.class);
            return retryMethodHandler.handle(target, method, objects);
        } catch (InvocationTargetException ex) {
            // 程序内部没有处理的异常
            throw ex.getTargetException();
        } catch (Throwable throwable) {
            throw throwable;
        }

    }
}
