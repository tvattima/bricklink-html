package com.bricklink.api.html.htmlunit;

public class HtmlUnitException extends RuntimeException {
    public HtmlUnitException() {
    }

    public HtmlUnitException(String message) {
        super(message);
    }

    public HtmlUnitException(String message, Throwable cause) {
        super(message, cause);
    }

    public HtmlUnitException(Throwable cause) {
        super(cause);
    }

    public HtmlUnitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
