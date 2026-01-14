package com.poldroc.retry.core.support.wait;

import com.poldroc.retry.api.model.WaitTime;
import com.poldroc.retry.api.support.wait.RetryWait;
import com.poldroc.retry.core.model.DefaultWaitTime;

/**
 * 默认重试时间等待
 *
 * @author Poldroc
 *  
 */

public abstract class AbstractRetryWait implements RetryWait {

    /**
     * 修正时间范围
     * 防止时间超出范围
     * @param timeMills 结果
     * @param min       最小值
     * @param max       最大值
     * @return 修正范围
     */
    protected WaitTime rangeCorrect(final long timeMills, final long min, final long max) {
        long resultMills = timeMills;
        if (timeMills > max) {
            resultMills = max;
        }
        if (timeMills < min) {
            resultMills = min;
        }
        return new DefaultWaitTime(resultMills);
    }


}
