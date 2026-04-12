package com.jarvis.deploy.deployment;

/**
 * Represents the possible states of a deployment.
 */
public enum DeploymentStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    ROLLED_BACK;

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == ROLLED_BACK;
    }

    public boolean isRollbackable() {
        return this == SUCCESS || this == FAILED;
    }
}
