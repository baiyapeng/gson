package com.google.gson;

/**
 * 循环引用异常
 *
 * @author baiyap
 * @date 2022-11-29 12:50:17
 */
public class CycleReferenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CycleReferenceException() {
    }

    public CycleReferenceException(String message) {
        super(message);
    }

    public CycleReferenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public CycleReferenceException(Throwable cause) {
        super(cause);
    }

    public CycleReferenceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
