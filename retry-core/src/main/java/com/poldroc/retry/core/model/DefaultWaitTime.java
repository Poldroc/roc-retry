package com.poldroc.retry.core.model;

import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.common.annotation.ThreadSafe;

import java.util.concurrent.TimeUnit;
/**
 * 默认等待时间
 * @author Poldroc
 *  
 */

@ThreadSafe
public class DefaultWaitTime implements WaitTime {

    /**
     * 等待时间
     */
    private final long time;

    /**
     * 时间单位
     */
    private final TimeUnit unit;

    public DefaultWaitTime(long time) {
        this.time = time;
        this.unit = TimeUnit.MILLISECONDS;
    }

    public DefaultWaitTime(long time, TimeUnit unit) {
        this.time = time;
        this.unit = unit;
    }

    @Override
    public long time() {
        return this.time;
    }

    @Override
    public TimeUnit unit() {
        return this.unit;
    }

}
