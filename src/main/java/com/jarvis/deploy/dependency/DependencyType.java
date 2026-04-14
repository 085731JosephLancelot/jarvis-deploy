package com.jarvis.deploy.dependency;

/**
 * Defines the types of dependency relationships between deployments.
 */
public enum DependencyType {

    /** The required deployment must be in SUCCESS state before source can start. */
    REQUIRES_SUCCESS,

    /** The required deployment must be completed (success or failed) before source can start. */
    REQUIRES_COMPLETION,

    /** The required deployment must be running before source can start. */
    REQUIRES_RUNNING
}
