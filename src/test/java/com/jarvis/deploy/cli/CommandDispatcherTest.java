package com.jarvis.deploy.cli;

import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.deployment.DeploymentStatus;
import com.jarvis.deploy.environment.Environment;
import com.jarvis.deploy.rollback.RollbackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommandDispatcherTest {

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private RollbackService rollbackService;

    @Mock
    private AuditLogger auditLogger;

    private CommandDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new CommandDispatcher(deploymentService, rollbackService, auditLogger);
    }

    @Test
    void deploy_withValidArgs_returnsSuccessMessage() {
        String result = dispatcher.dispatch("deploy", new String[]{"prod", "1.2.3", "my-app.jar"});

        assertThat(result).contains("my-app.jar").contains("1.2.3").contains("prod");
        verify(deploymentService).deploy(any(Environment.class), eq("1.2.3"), eq("my-app.jar"));
        verify(auditLogger).log(eq("deploy"), anyString());
    }

    @Test
    void deploy_withInsufficientArgs_returnsUsageMessage() {
        String result = dispatcher.dispatch("deploy", new String[]{"prod"});

        assertThat(result).startsWith("Usage: deploy");
        verifyNoInteractions(deploymentService);
    }

    @Test
    void rollback_withValidArgs_returnsSuccessMessage() {
        String result = dispatcher.dispatch("rollback", new String[]{"staging", "dep-42"});

        assertThat(result).contains("dep-42").contains("staging");
        verify(rollbackService).rollback(any(Environment.class), eq("dep-42"));
        verify(auditLogger).log(eq("rollback"), anyString());
    }

    @Test
    void status_withValidEnv_returnsStatus() {
        when(deploymentService.getStatus(any(Environment.class))).thenReturn(DeploymentStatus.SUCCESS);

        String result = dispatcher.dispatch("status", new String[]{"dev"});

        assertThat(result).isNotBlank();
        verify(deploymentService).getStatus(any(Environment.class));
    }

    @Test
    void help_returnsAvailableCommands() {
        String result = dispatcher.dispatch("help", new String[]{});

        assertThat(result).contains("deploy").contains("rollback").contains("status");
    }

    @Test
    void unknownCommand_returnsErrorMessage() {
        String result = dispatcher.dispatch("launch", new String[]{});

        assertThat(result).contains("Unknown command").contains("launch");
    }

    @Test
    void dispatch_whenServiceThrows_returnsErrorAndLogsAudit() {
        doThrow(new RuntimeException("connection refused"))
            .when(deploymentService).deploy(any(), anyString(), anyString());

        String result = dispatcher.dispatch("deploy", new String[]{"prod", "1.0.0", "app.jar"});

        assertThat(result).contains("Error").contains("deploy");
        verify(auditLogger).log(eq("error"), anyString());
    }
}
