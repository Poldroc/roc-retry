package com.poldroc.retry.common.support.proxy;

import com.poldroc.retry.common.constant.enums.ProxyTypeEnum;

import java.lang.reflect.Proxy;

public class ProxyFactory {
    private ProxyFactory() {
    }

    public static ProxyTypeEnum getProxyType(Object object) {
        if (object == null) {
            return ProxyTypeEnum.NONE;
        } else {
            Class clazz = object.getClass();
            return !clazz.isInterface() && !Proxy.isProxyClass(clazz) ? ProxyTypeEnum.CGLIB : ProxyTypeEnum.DYNAMIC;
        }
    }
}
