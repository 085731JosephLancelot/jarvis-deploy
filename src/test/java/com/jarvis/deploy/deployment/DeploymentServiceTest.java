package com.jarvis.deploy.deployment;

import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.environment.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentServiceTest {

    private DeploymentService deploymentService;
    private Environment validEnv;
    private AuditLogger auditLogger;

    @BeforeEach
    void setUp() {
        auditLogger = new AuditLogger();
        deploymentService = new DeploymentService(auditLogger);
        validEnv = new Environment("production", "http://prod.example.com", true);
    }

    @Test
    void shouldDeploySuccessfullyToValidEnvironment() {
        Deployment result = deploymentService.deploy("1.2.0", validEnv);
        assertNotNull(result);
        assertEquals(DeploymentStatus.SUCCESS, result.getStatus());
        assertEquals("1.2.0", result.getArtifactVersion());
    }

    @Test
    void shouldRecordDeploymentInHistory() {
        deploymentService.deploy("1.0.0", validEnv);
        deploymentService.deploy("1.1.0", validEnv);
        assertEquals(2, deploymentService.getHistory().size());
    }

    @Test
    void shouldRollbackSuccessfulDeployment() {
        Deployment deployment = deploymentService.deploy("1.0.0", validEnv);
        Optional<Deployment> rolled = deploymentService.rollback(deployment.getId());
        assertTrue(rolled.isPresent());
        assertEquals(DeploymentStatus.ROLLED_BACK, rolled.get().getStatus());
    }

    @Test
    void shouldReturnEmptyWhenRollbackIdNotFound() {
        Optional<Deployment> result = deploymentService.rollback("nonexistent-id");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenRollbackAlreadyRolledBack() {
        Deployment deployment = deploymentService.deploy("1.0.0", validEnv);
        deploymentService.rollback(deployment.getId());
        Optional<Deployment> second = deploymentService.rollback(deployment.getId());
        assertFalse(second.isPresent());
    }

    @Test
    void shouldFilterHistoryByEnvironment() {
        Environment otherEnv = new Environment("staging", "http://staging.example.com", false);
        deploymentService.deploy("1.0.0", validEnv);
        deploymentService.deploy("1.0.1", otherEnv);
        assertEquals(1, deploymentService.getHistoryForEnvironment(validEnv).size());
    }
}
