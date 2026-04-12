package com.jarvis.deploy.health;

import com.jarvis.deploy.environment.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HealthCheckerTest {

    private HealthChecker healthChecker;
    private Environment activeEnvironment;
    private Environment inactiveEnvironment;

    @BeforeEach
    void setUp() {
        healthChecker = new HealthChecker(10, 3);

        activeEnvironment = mock(Environment.class);
        when(activeEnvironment.getName()).thenReturn("staging");
        when(activeEnvironment.getRegion()).thenReturn("us-east-1");
        when(activeEnvironment.isActive()).thenReturn(true);

        inactiveEnvironment = mock(Environment.class);
        when(inactiveEnvironment.getName()).thenReturn("legacy");
        when(inactiveEnvironment.getRegion()).thenReturn("eu-west-1");
        when(inactiveEnvironment.isActive()).thenReturn(false);
    }

    @Test
    void constructorRejectsNonPositiveTimeout() {
        assertThrows(IllegalArgumentException.class, () -> new HealthChecker(0, 3));
        assertThrows(IllegalArgumentException.class, () -> new HealthChecker(-5, 3));
    }

    @Test
    void constructorRejectsNegativeRetries() {
        assertThrows(IllegalArgumentException.class, () -> new HealthChecker(10, -1));
    }

    @Test
    void checkThrowsOnNullEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> healthChecker.check(null));
    }

    @Test
    void checkReturnsHealthyForActiveEnvironment() {
        HealthCheckResult result = healthChecker.check(activeEnvironment);

        assertNotNull(result);
        assertTrue(result.isHealthy());
        assertEquals("staging", result.getEnvironmentName());
        assertTrue(result.getMessage().contains("healthy"));
    }

    @Test
    void checkReturnsUnhealthyForInactiveEnvironment() {
        HealthCheckResult result = healthChecker.check(inactiveEnvironment);

        assertNotNull(result);
        assertFalse(result.isHealthy());
        assertEquals("legacy", result.getEnvironmentName());
        assertTrue(result.getMessage().contains("failed"));
    }

    @Test
    void checkResultContainsEnvironmentDetails() {
        HealthCheckResult result = healthChecker.check(activeEnvironment);

        assertNotNull(result.getDetails());
        assertEquals("staging", result.getDetails().get("environment"));
        assertEquals("us-east-1", result.getDetails().get("region"));
    }

    @Test
    void gettersReturnConfiguredValues() {
        assertEquals(10, healthChecker.getTimeoutSeconds());
        assertEquals(3, healthChecker.getMaxRetries());
    }
}
