package com.poldroc.retry.annotation.model;

import com.poldroc.retry.annotation.annotation.metadata.RetryAble;

import java.lang.annotation.Annotation;

/**
 * 可重试注解对象
 *
 * @author Poldroc
 * @date 2024/7/13
 */
public class RetryAbleBean {

    /**
     * 注解信息
     */
    private RetryAble retryAble;

    /**
     * 原始注解信息
     * @see com.poldroc.retry.annotation.annotation.Retry
     */
    private Annotation annotation;

    /**
     * 请求参数
     */
    private Object[] args;

    public RetryAble retryAble() {
        return retryAble;
    }

    public RetryAbleBean retryAble(RetryAble retryAble) {
        this.retryAble = retryAble;
        return this;
    }

    public Annotation annotation() {
        return annotation;
    }

    public RetryAbleBean annotation(Annotation annotation) {
        this.annotation = annotation;
        return this;
    }

    public Object[] args() {
        return args;
    }

    public RetryAbleBean args(Object[] args) {
        this.args = args;
        return this;
    }
}
