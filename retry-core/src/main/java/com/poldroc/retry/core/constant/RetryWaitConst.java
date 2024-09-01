package com.poldroc.retry.core.constant;

/**
 * 重试等待时间常量
 *
 * @author Poldroc
 * @since 2024/7/11
 */

public final class RetryWaitConst {
    private RetryWaitConst() {
    }

    /**
     * 默认基础值
     * 1s
     */
    public static final long DEFAULT_VALUE_MILLS = 1000L;

    /**
     * 最小等待时间
     */
    public static final long DEFAULT_MIN_MILLS = 0L;

    /**
     * 最大等待时间
     * 30min
     */
    public static final long DEFAULT_MAX_MILLS = 30 * 60 * 1000L;

    /**
     * 增加的毫秒数因数
     * 默认为 2S
     */
    public static final double INCREASE_MILLS_FACTOR = 2000;

    /**
     * 因数
     * 默认为黄金分割比
     */
    public static final double MULTIPLY_FACTOR = 1.618;


}
