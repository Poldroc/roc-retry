package com.poldroc.retry.api.model;

import java.util.Date;
/**
 * 尝试执行时间接口
 * @author Poldroc
 * @since 2024/7/11
 */

public interface AttemptTime {

    /**
     * 开始时间
     * @return 开始时间
     */
    Date startTime();

    /**
     * 结束时间
     * @return 结束时间
     */
    Date endTime();

    /**
     * 消耗的时间(毫秒)
     * @return 消耗的时间
     */
    long costTimeInMills();

}
