package me.lucko.helper.exception;

public class HelperException extends Exception {
    public HelperException(String message) {
        super(message);
    }

    public HelperException(String message, Throwable cause) {
        super(message, cause);
    }

    public HelperException(Throwable cause) {
        super(cause);
    }

    protected HelperException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
