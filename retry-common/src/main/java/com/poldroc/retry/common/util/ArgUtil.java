package com.poldroc.retry.common.util;
/**
 * 参数工具类
 * @author Poldroc
 * @date 2024/7/11
 */

public final class ArgUtil {

    private ArgUtil() {
    }

    /**
     * 断言不为空
     *
     * @param object 对象
     * @param name   对象名称
     */
    public static void notNull(Object object, String name) {
        if (null == object) {
            throw new IllegalArgumentException(name + " can not be null!");
        }
    }

    /**
     * 校验字符串非空
     * @param string 待检查的字符串
     * @param name 字符串的名称
     */
    public static void notEmpty(String string, String name) {
        if(StringUtil.isEmpty(string)) {
            throw new IllegalArgumentException(name + " can not be null!");
        }
    }
}
