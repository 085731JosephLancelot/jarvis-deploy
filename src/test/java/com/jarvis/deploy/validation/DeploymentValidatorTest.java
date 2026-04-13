package com.jarvis.deploy.validation;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;
import com.jarvis.deploy.environment.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentValidatorTest {

    private DeploymentValidator validator;
    private Deployment deployment;
    private Environment environment;

    @BeforeEach
    void setUp() {
        validator = new DeploymentValidator();
        deployment = mock(Deployment.class);
        environment = mock(Environment.class);

        when(deployment.getAppName()).thenReturn("my-app");
        when(deployment.getVersion()).thenReturn("1.0.0");
        when(deployment.getEnvironmentName()).thenReturn("production");
        when(deployment.getStatus()).thenReturn(DeploymentStatus.PENDING);
        when(environment.getName()).thenReturn("production");
    }

    @Test
    void validDeploymentPassesValidation() {
        ValidationResult result = validator.validate(deployment, environment);
        assertTrue(result.isValid());
        assertTrue(result.getViolations().isEmpty());
    }

    @Test
    void blankAppNameFailsValidation() {
        when(deployment.getAppName()).thenReturn("");
        ValidationResult result = validator.validate(deployment, environment);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("Application name")));
    }

    @Test
    void invalidAppNameCharactersFailsValidation() {
        when(deployment.getAppName()).thenReturn("my app!");
        ValidationResult result = validator.validate(deployment, environment);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("invalid characters")));
    }

    @Test
    void blankVersionFailsValidation() {
        when(deployment.getVersion()).thenReturn("  ");
        ValidationResult result = validator.validate(deployment, environment);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("Version")));
    }

    @Test
    void environmentMismatchFailsValidation() {
        when(deployment.getEnvironmentName()).thenReturn("staging");
        ValidationResult result = validator.validate(deployment, environment);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("does not match")));
    }

    @Test
    void nullStatusFailsValidation() {
        when(deployment.getStatus()).thenReturn(null);
        ValidationResult result = validator.validate(deployment, environment);
        assertFalse(result.isValid());
        assertTrue(result.getViolations().stream().anyMatch(v -> v.contains("status")));
    }

    @Test
    void summaryContainsAllViolations() {
        when(deployment.getAppName()).thenReturn("");
        when(deployment.getVersion()).thenReturn("");
        ValidationResult result = validator.validate(deployment, environment);
        String summary = result.getSummary();
        assertTrue(summary.contains("violation"));
        assertFalse(result.isValid());
    }

    @Test
    void nullDeploymentThrowsException() {
        assertThrows(NullPointerException.class, () -> validator.validate(null, environment));
    }

    @Test
    void nullEnvironmentThrowsException() {
        assertThrows(NullPointerException.class, () -> validator.validate(deployment, null));
    }
}
