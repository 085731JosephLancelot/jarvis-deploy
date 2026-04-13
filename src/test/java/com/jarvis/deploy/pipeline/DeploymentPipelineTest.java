package com.jarvis.deploy.pipeline;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeploymentPipelineTest {

    private DeploymentPipeline pipeline;
    private Deployment mockDeployment;

    @BeforeEach
    void setUp() {
        pipeline = new DeploymentPipeline("pipe-001", "staging");
        mockDeployment = mock(Deployment.class);
        when(mockDeployment.getDeploymentId()).thenReturn("deploy-42");
    }

    @Test
    void constructor_setsInitialStatusToPending() {
        assertEquals(PipelineStatus.PENDING, pipeline.getStatus());
        assertEquals("pipe-001", pipeline.getPipelineId());
        assertEquals("staging", pipeline.getEnvironment());
        assertTrue(pipeline.getStages().isEmpty());
    }

    @Test
    void constructor_throwsOnNullId() {
        assertThrows(NullPointerException.class, () -> new DeploymentPipeline(null, "prod"));
    }

    @Test
    void constructor_throwsOnNullEnvironment() {
        assertThrows(NullPointerException.class, () -> new DeploymentPipeline("p1", null));
    }

    @Test
    void addStage_appendsStageToList() {
        PipelineStage stage = new PipelineStage("build", 0);
        pipeline.addStage(stage);
        assertEquals(1, pipeline.getStages().size());
        assertEquals("build", pipeline.getStages().get(0).getName());
    }

    @Test
    void addStage_throwsOnNull() {
        assertThrows(NullPointerException.class, () -> pipeline.addStage(null));
    }

    @Test
    void start_setsStatusToRunningAndRecordsTime() {
        pipeline.start(mockDeployment);
        assertEquals(PipelineStatus.RUNNING, pipeline.getStatus());
        assertNotNull(pipeline.getStartedAt());
        assertEquals(mockDeployment, pipeline.getDeployment());
    }

    @Test
    void complete_setsStatusAndCompletionTime() {
        pipeline.start(mockDeployment);
        pipeline.complete();
        assertEquals(PipelineStatus.COMPLETED, pipeline.getStatus());
        assertNotNull(pipeline.getCompletedAt());
    }

    @Test
    void fail_setsFailedStatusAndCompletionTime() {
        pipeline.start(mockDeployment);
        pipeline.fail();
        assertEquals(PipelineStatus.FAILED, pipeline.getStatus());
        assertNotNull(pipeline.getCompletedAt());
    }

    @Test
    void stages_listIsUnmodifiable() {
        pipeline.addStage(new PipelineStage("test", 1));
        assertThrows(UnsupportedOperationException.class,
                () -> pipeline.getStages().add(new PipelineStage("extra", 2)));
    }

    @Test
    void pipelineStage_lifecycleTransitions() {
        PipelineStage stage = new PipelineStage("deploy", 2);
        assertEquals(PipelineStatus.PENDING, stage.getStatus());
        stage.markRunning();
        assertEquals(PipelineStatus.RUNNING, stage.getStatus());
        assertNotNull(stage.getStartedAt());
        stage.markCompleted("success");
        assertEquals(PipelineStatus.COMPLETED, stage.getStatus());
        assertEquals("success", stage.getResultMessage());
        assertNotNull(stage.getFinishedAt());
    }

    @Test
    void pipelineStage_markFailed_setsMessage() {
        PipelineStage stage = new PipelineStage("verify", 3);
        stage.markRunning();
        stage.markFailed("health check timed out");
        assertEquals(PipelineStatus.FAILED, stage.getStatus());
        assertEquals("health check timed out", stage.getResultMessage());
    }

    @Test
    void pipelineStage_negativeOrderThrows() {
        assertThrows(IllegalArgumentException.class, () -> new PipelineStage("bad", -1));
    }
}
