package com.poldroc.retry.annotation.core;

import com.poldroc.retry.annotation.proxy.cglib.CglibProxy;
import com.poldroc.retry.annotation.proxy.dynamic.DynamicProxy;
import com.poldroc.retry.annotation.proxy.none.NoneProxy;
import com.poldroc.retry.common.constant.enums.ProxyTypeEnum;
import com.poldroc.retry.common.support.proxy.ProxyFactory;

/**
 * 重试模板
 *
 * @author Poldroc
 * @since 2024/7/18
 */

public class RetryTemplate {

    private RetryTemplate() {
    }

    public static <R> R getProxyObject(R object) {
        ProxyTypeEnum proxyType = ProxyFactory.getProxyType(object);
        if (ProxyTypeEnum.NONE.equals(proxyType)) {
            return (R) new NoneProxy(object).proxy();
        } else if (ProxyTypeEnum.DYNAMIC.equals(proxyType)) {
            return (R) new DynamicProxy(object).proxy();
        } else {
            return (R) new CglibProxy(object).proxy();
        }
    }
}
