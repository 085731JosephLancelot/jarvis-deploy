package com.jarvis.deploy.quota;

/**
 * Defines the scope at which a deployment quota is enforced.
 */
public enum QuotaScope {
    /** Quota applies globally across all environments. */
    GLOBAL,
    /** Quota applies per environment. */
    ENVIRONMENT,
    /** Quota applies per service/application. */
    SERVICE,
    /** Quota applies per user or principal. */
    USER
}
