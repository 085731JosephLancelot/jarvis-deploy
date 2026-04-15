package com.jarvis.deploy.plugin;

/**
 * Enumerates the supported plugin hook types in the deployment lifecycle.
 */
public enum PluginType {
    PRE_DEPLOY,
    POST_DEPLOY,
    PRE_ROLLBACK,
    POST_ROLLBACK,
    HEALTH_CHECK,
    NOTIFICATION
}
