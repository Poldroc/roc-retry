package com.poldroc.retry.core.support.recover;

import com.poldroc.retry.api.support.recover.Recover;
/**
 * 没有任何恢复操作
 * @author Poldroc
 *  
 */

public final class Recovers {

    private Recovers(){}

    /**
     * 没有任何恢复操作实例
     * @return recover 实例
     */
    public static Recover noRecover() {
        return NoRecover.getInstance();
    }

}
