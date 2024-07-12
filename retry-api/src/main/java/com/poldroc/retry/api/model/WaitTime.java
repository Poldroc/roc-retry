package com.poldroc.retry.api.model;

import java.util.concurrent.TimeUnit;
/**
 * 等待时间接口
 * @author Poldroc
 * @date 2024/7/11
 */

public interface WaitTime {

    /**
     * 等待时间
     * @return 时间
     */
    long time();

    /**
     * 等待时间单位
     * @return 时间单位
     */
    TimeUnit unit();

}
