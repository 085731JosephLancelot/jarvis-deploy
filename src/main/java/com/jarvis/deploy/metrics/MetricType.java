package com.jarvis.deploy.metrics;

/**
 * Enum representing the types of deployment metrics tracked by Jarvis.
 */
public enum MetricType {
    DEPLOYMENT_DURATION,
    ROLLBACK_COUNT,
    SUCCESS_RATE,
    FAILURE_COUNT,
    HEALTH_CHECK_LATENCY,
    PIPELINE_STAGE_DURATION
}
