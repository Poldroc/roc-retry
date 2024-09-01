package com.poldroc.retry.test.support.recover;


import com.poldroc.retry.api.model.RetryAttempt;
import com.poldroc.retry.api.support.recover.Recover;
/**
 * 自定义恢复策略
 * @author Poldroc
 * @since 2024/7/22
 */

public class MyRecover implements Recover {

    @Override
    public <R> void recover(RetryAttempt<R> retryAttempt) {
        Object[] params = retryAttempt.params();

        String name = params[0].toString();
        // 通知
        System.out.println("[Recover] " + name + "查询失败了！");
    }

}
