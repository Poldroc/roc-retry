package com.poldroc.retry.annotation.proxy;

import java.lang.reflect.Method;
public interface IMethodHandler {
    Object handle(Object var1, Method var2, Object[] var3) throws Throwable;
}
