package com.bricklink.web;

public class BricklinkWebException extends RuntimeException {
    public BricklinkWebException() {
        super();
    }

    public BricklinkWebException(String message) {
        super(message);
    }

    public BricklinkWebException(String message, Throwable cause) {
        super(message, cause);
    }

    public BricklinkWebException(Throwable cause) {
        super(cause);
    }

    protected BricklinkWebException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
