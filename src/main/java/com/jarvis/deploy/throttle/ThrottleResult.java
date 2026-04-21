package com.jarvis.deploy.throttle;

/**
 * Represents the outcome of a throttle acquisition attempt.
 */
public class ThrottleResult {

    public enum Status { ALLOWED, WARNED, DENIED }

    private final Status status;
    private final String message;

    private ThrottleResult(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public static ThrottleResult allowed(String message) {
        return new ThrottleResult(Status.ALLOWED, message);
    }

    public static ThrottleResult warned(String message) {
        return new ThrottleResult(Status.WARNED, message);
    }

    public static ThrottleResult denied(String message) {
        return new ThrottleResult(Status.DENIED, message);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public boolean isAllowed() {
        return status == Status.ALLOWED || status == Status.WARNED;
    }

    public boolean isDenied() {
        return status == Status.DENIED;
    }

    @Override
    public String toString() {
        return "ThrottleResult{status=" + status + ", message='" + message + "'}";
    }
}
