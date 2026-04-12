package com.jarvis.deploy.deployment;

import com.jarvis.deploy.environment.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTest {

    private Environment environment;

    @BeforeEach
    void setUp() {
        environment = new Environment("staging", "http://staging.example.com", false);
    }

    @Test
    void shouldCreateDeploymentWithPendingStatus() {
        Deployment deployment = new Deployment("1.0.0", environment);
        assertNotNull(deployment.getId());
        assertEquals("1.0.0", deployment.getArtifactVersion());
        assertEquals(DeploymentStatus.PENDING, deployment.getStatus());
        assertNotNull(deployment.getCreatedAt());
        assertNull(deployment.getCompletedAt());
    }

    @Test
    void shouldSetCompletedAtWhenStatusIsTerminal() {
        Deployment deployment = new Deployment("1.0.0", environment);
        deployment.setStatus(DeploymentStatus.SUCCESS);
        assertNotNull(deployment.getCompletedAt());
    }

    @Test
    void shouldNotSetCompletedAtForNonTerminalStatus() {
        Deployment deployment = new Deployment("1.0.0", environment);
        deployment.setStatus(DeploymentStatus.IN_PROGRESS);
        assertNull(deployment.getCompletedAt());
    }

    @Test
    void shouldThrowWhenArtifactVersionIsNull() {
        assertThrows(NullPointerException.class, () -> new Deployment(null, environment));
    }

    @Test
    void shouldThrowWhenEnvironmentIsNull() {
        assertThrows(NullPointerException.class, () -> new Deployment("1.0.0", null));
    }

    @Test
    void toStringShouldContainKeyInfo() {
        Deployment deployment = new Deployment("2.3.1", environment);
        String str = deployment.toString();
        assertTrue(str.contains("2.3.1"));
        assertTrue(str.contains("staging"));
        assertTrue(str.contains("PENDING"));
    }
}
