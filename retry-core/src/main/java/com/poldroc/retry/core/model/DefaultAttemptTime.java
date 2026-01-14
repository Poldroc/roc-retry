package com.poldroc.retry.core.model;

import com.poldroc.retry.api.model.AttemptTime;
import com.poldroc.retry.common.annotation.NotThreadSafe;

import java.util.Date;

/**
 * 尝试执行的时候消耗时间
 * @author Poldroc
 *  
 */

@NotThreadSafe
public class DefaultAttemptTime implements AttemptTime {

    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 消耗的时间
     */
    private long costTimeInMills;

    @Override
    public Date startTime() {
        return startTime;
    }

    public DefaultAttemptTime startTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    @Override
    public Date endTime() {
        return endTime;
    }

    public DefaultAttemptTime endTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    @Override
    public long costTimeInMills() {
        return costTimeInMills;
    }

    public DefaultAttemptTime costTimeInMills(long costTimeInMills) {
        this.costTimeInMills = costTimeInMills;
        return this;
    }

    @Override
    public String toString() {
        return "DefaultAttemptTime{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", costTimeInMills=" + costTimeInMills +
                '}';
    }
}
