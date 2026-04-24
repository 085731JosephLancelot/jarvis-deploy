package com.jarvis.deploy.lease;

/**
 * Thrown when a lease cannot be acquired because an active lease already exists
 * for the target environment.
 */
public class LeaseConflictException extends RuntimeException {

    public LeaseConflictException(String message) {
        super(message);
    }

    public LeaseConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
