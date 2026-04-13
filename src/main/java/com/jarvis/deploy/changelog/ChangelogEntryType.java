package com.jarvis.deploy.changelog;

/**
 * Categorises the kind of change recorded in the deployment changelog.
 */
public enum ChangelogEntryType {
    DEPLOY,
    ROLLBACK,
    CONFIG_CHANGE,
    SCALE,
    HOTFIX,
    SCHEDULED
}
