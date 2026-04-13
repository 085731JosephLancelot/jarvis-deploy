package com.jarvis.deploy.validation;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.environment.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates a deployment before execution, collecting all constraint violations.
 */
public class DeploymentValidator {

    private static final int MAX_VERSION_LENGTH = 64;
    private static final int MAX_APP_NAME_LENGTH = 128;

    public ValidationResult validate(Deployment deployment, Environment environment) {
        Objects.requireNonNull(deployment, "Deployment must not be null");
        Objects.requireNonNull(environment, "Environment must not be null");

        List<String> violations = new ArrayList<>();

        validateAppName(deployment.getAppName(), violations);
        validateVersion(deployment.getVersion(), violations);
        validateEnvironmentMatch(deployment, environment, violations);
        validateNotNullStatus(deployment, violations);

        return new ValidationResult(violations);
    }

    private void validateAppName(String appName, List<String> violations) {
        if (appName == null || appName.isBlank()) {
            violations.add("Application name must not be blank");
        } else if (appName.length() > MAX_APP_NAME_LENGTH) {
            violations.add("Application name exceeds maximum length of " + MAX_APP_NAME_LENGTH);
        } else if (!appName.matches("[a-zA-Z0-9_\\-]+")) {
            violations.add("Application name contains invalid characters (allowed: a-z, A-Z, 0-9, _, -)");
        }
    }

    private void validateVersion(String version, List<String> violations) {
        if (version == null || version.isBlank()) {
            violations.add("Version must not be blank");
        } else if (version.length() > MAX_VERSION_LENGTH) {
            violations.add("Version exceeds maximum length of " + MAX_VERSION_LENGTH);
        } else if (!version.matches("[a-zA-Z0-9._\\-]+")) {
            violations.add("Version contains invalid characters");
        }
    }

    private void validateEnvironmentMatch(Deployment deployment, Environment environment, List<String> violations) {
        if (deployment.getEnvironmentName() == null) {
            violations.add("Deployment environment name must not be null");
        } else if (!deployment.getEnvironmentName().equalsIgnoreCase(environment.getName())) {
            violations.add("Deployment environment '" + deployment.getEnvironmentName()
                    + "' does not match target environment '" + environment.getName() + "'");
        }
    }

    private void validateNotNullStatus(Deployment deployment, List<String> violations) {
        if (deployment.getStatus() == null) {
            violations.add("Deployment status must not be null");
        }
    }
}
