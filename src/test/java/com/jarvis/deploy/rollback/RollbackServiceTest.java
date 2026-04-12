package com.jarvis.deploy.rollback;

import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RollbackServiceTest {

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private AuditLogger auditLogger;

    private RollbackService rollbackService;

    @BeforeEach
    void setUp() {
        rollbackService = new RollbackService(deploymentService, auditLogger);
    }

    @Test
    void rollback_shouldRestorePreviousSuccessfulDeployment() {
        Deployment current = new Deployment("prod", "2.0.0", DeploymentStatus.SUCCESS);
        Deployment previous = new Deployment("prod", "1.9.0", DeploymentStatus.SUCCESS);
        List<Deployment> history = Arrays.asList(current, previous);

        when(deploymentService.getDeploymentHistory("prod")).thenReturn(history);

        Deployment result = rollbackService.rollback("prod");

        assertThat(result.getVersion()).isEqualTo("1.9.0");
        verify(deploymentService).promoteDeployment(eq("prod"), eq("1.9.0"));
        verify(auditLogger).log(any());
    }

    @Test
    void rollback_shouldThrowWhenNoSuccessfulDeploymentExists() {
        Deployment failed = new Deployment("staging", "1.0.0", DeploymentStatus.FAILED);
        when(deploymentService.getDeploymentHistory("staging")).thenReturn(List.of(failed));

        assertThatThrownBy(() -> rollbackService.rollback("staging"))
                .isInstanceOf(RollbackException.class)
                .hasMessageContaining("No successful deployment found");

        verify(deploymentService, never()).promoteDeployment(any(), any());
    }

    @Test
    void rollback_shouldThrowWhenNoPreviousVersionAvailable() {
        Deployment only = new Deployment("dev", "1.0.0", DeploymentStatus.SUCCESS);
        when(deploymentService.getDeploymentHistory("dev")).thenReturn(Collections.singletonList(only));

        assertThatThrownBy(() -> rollbackService.rollback("dev"))
                .isInstanceOf(RollbackException.class)
                .hasMessageContaining("No previous successful deployment");

        verify(deploymentService, never()).promoteDeployment(any(), any());
    }
}
