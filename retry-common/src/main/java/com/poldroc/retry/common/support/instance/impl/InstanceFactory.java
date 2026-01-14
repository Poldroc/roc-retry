package com.poldroc.retry.common.support.instance.impl;

import com.poldroc.retry.common.annotation.ThreadSafe;
import com.poldroc.retry.common.support.instance.Instance;
import com.poldroc.retry.common.util.ArgUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实例化工厂类
 *
 * @author Poldroc
 *  
 */
@ThreadSafe
public class InstanceFactory implements Instance {

    private InstanceFactory() {
    }

    /**
     * 单例 map 对象
     * key 是 class 的全称
     */
    private final Map<String, Object> singletonMap = new ConcurrentHashMap<>();

    /**
     * 线程内的 map 对象
     */
    private ThreadLocal<Map<String, Object>> mapThreadLocal = new ThreadLocal<>();

    /**
     * 静态内部类实现单例
     */
    private static class SingletonHolder {
        private static final InstanceFactory INSTANCE_FACTORY = new InstanceFactory();
    }

    /**
     * 获取单例对象
     *
     * @return 实例化对象
     */
    public static InstanceFactory getInstance() {
        return SingletonHolder.INSTANCE_FACTORY;
    }

    @Override
    public <T> T singleton(Class<T> tClass, String groupName) {
        return getSingleton(tClass, groupName, singletonMap);
    }

    @Override
    public <T> T singleton(Class<T> tClass) {
        this.notNull(tClass);
        return this.getSingleton(tClass, singletonMap);
    }

    @Override
    public <T> T threadLocal(Class<T> tClass) {
        this.notNull(tClass);
        Map<String, Object> map = mapThreadLocal.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }
        T instance = this.getSingleton(tClass, map);
        mapThreadLocal.set(map);
        return instance;
    }

    @Override
    public <T> T multiple(Class<T> tClass) {
        this.notNull(tClass);
        try {
            return tClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Create instance failed.", e);
        }
    }

    @Override
    public <T> T threadSafe(Class<T> tClass) {
        if (tClass.isAnnotationPresent(ThreadSafe.class)) {
            return this.singleton(tClass);
        }
        return this.multiple(tClass);
    }

    /**
     * 获取单例对象
     *
     * @param tClass      class 类型
     * @param instanceMap 实例化对象 map
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    private <T> T getSingleton(final Class<T> tClass, final Map<String, Object> instanceMap) {
        this.notNull(tClass);

        final String fullClassName = tClass.getName();
        T instance = (T) instanceMap.get(fullClassName);
        if (instance == null) {
            instance = this.multiple(tClass);
            instanceMap.put(fullClassName, instance);
        }
        return instance;
    }

    /**
     * 获取单例对象
     *
     * @param tClass      查询 tClass
     * @param group       分组信息
     * @param instanceMap 实例化对象 map
     * @return 单例对象
     */
    @SuppressWarnings("unchecked")
    private <T> T getSingleton(final Class<T> tClass,
                               final String group, final Map<String, Object> instanceMap) {
        this.notNull(tClass);
        ArgUtil.notEmpty(group, "key");

        final String fullClassName = tClass.getName() + "-" + group;
        T instance = (T) instanceMap.get(fullClassName);
        if (instance == null) {
            instance = this.multiple(tClass);
            instanceMap.put(fullClassName, instance);
        }
        return instance;
    }

    /**
     * 断言参数不可为 null
     *
     * @param tClass class 信息
     */
    private void notNull(final Class tClass) {
        ArgUtil.notNull(tClass, "class");
    }

}
