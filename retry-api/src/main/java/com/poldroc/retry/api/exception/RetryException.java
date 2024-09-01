package com.poldroc.retry.api.exception;

/**
 * 重试异常
 *
 * @author Poldroc
 * @since 2024/7/11
 */

public class RetryException extends RuntimeException {

    public RetryException() {
    }

    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryException(Throwable cause) {
        super(cause);
    }

    public RetryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
