package com.poldroc.retry.common.util;

/**
 * 字符串工具类
 *
 * @author Poldroc
 * @date 2024/7/11
 */

public final class StringUtil {

    private StringUtil() {
    }


    /**
     * 空字符串
     */
    public static final String EMPTY = "";


    /**
     * 判断字符串是否为空
     *
     * @param string 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String string) {
        return null == string || EMPTY.equals(string);
    }
}
