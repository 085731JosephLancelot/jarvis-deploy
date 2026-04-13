package com.jarvis.deploy.quota;

/**
 * Thrown when a deployment quota has been exceeded for an environment.
 */
public class QuotaExceededException extends RuntimeException {

    public QuotaExceededException(String message) {
        super(message);
    }

    public QuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
