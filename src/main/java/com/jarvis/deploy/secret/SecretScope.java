package com.jarvis.deploy.secret;

/**
 * Defines the scope at which a secret is accessible.
 */
public enum SecretScope {
    /** Available across all environments */
    GLOBAL,
    /** Available only within a specific environment */
    ENVIRONMENT,
    /** Available only for a specific deployment */
    DEPLOYMENT
}
