package com.jarvis.deploy.webhook;

/**
 * Enumeration of deployment webhook event types.
 */
public enum WebhookEventType {
    DEPLOYMENT_STARTED,
    DEPLOYMENT_SUCCEEDED,
    DEPLOYMENT_FAILED,
    ROLLBACK_INITIATED,
    ROLLBACK_COMPLETED,
    HEALTH_CHECK_PASSED,
    HEALTH_CHECK_FAILED,
    APPROVAL_REQUESTED,
    APPROVAL_GRANTED,
    APPROVAL_REJECTED
}
