package com.jarvis.deploy.canary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CanaryControllerTest {

    private CanaryController controller;
    private CanaryPolicy defaultPolicy;

    @BeforeEach
    void setUp() {
        controller = new CanaryController();
        defaultPolicy = CanaryPolicy.builder("standard-canary")
                .initialTrafficPercent(10)
                .targetTrafficPercent(100)
                .stepPercent(10)
                .stepInterval(Duration.ofMinutes(2))
                .errorRateThreshold(0.05)
                .build();
    }

    @Test
    void startRegistersCanaryAndSetsInitialPercent() {
        controller.start("deploy-1", defaultPolicy);
        assertTrue(controller.isActive("deploy-1"));
        assertEquals(10, controller.getCurrentTrafficPercent("deploy-1"));
    }

    @Test
    void startThrowsWhenAlreadyActive() {
        controller.start("deploy-1", defaultPolicy);
        assertThrows(IllegalStateException.class, () -> controller.start("deploy-1", defaultPolicy));
    }

    @Test
    void stepAdvancesTrafficPercent() {
        controller.start("deploy-1", defaultPolicy);
        CanaryController.CanaryState state = controller.step("deploy-1", 0.01);
        assertEquals(CanaryController.CanaryState.RUNNING, state);
        assertEquals(20, controller.getCurrentTrafficPercent("deploy-1"));
    }

    @Test
    void stepAbortsWhenErrorRateExceedsThreshold() {
        controller.start("deploy-1", defaultPolicy);
        CanaryController.CanaryState state = controller.step("deploy-1", 0.10);
        assertEquals(CanaryController.CanaryState.ABORTED, state);
        assertFalse(controller.isActive("deploy-1"));
    }

    @Test
    void stepPromotesWhenTargetReached() {
        CanaryPolicy fastPolicy = CanaryPolicy.builder("fast")
                .initialTrafficPercent(90)
                .targetTrafficPercent(100)
                .stepPercent(10)
                .stepInterval(Duration.ofSeconds(30))
                .errorRateThreshold(0.05)
                .build();
        controller.start("deploy-2", fastPolicy);
        CanaryController.CanaryState state = controller.step("deploy-2", 0.00);
        assertEquals(CanaryController.CanaryState.PROMOTED, state);
        assertFalse(controller.isActive("deploy-2"));
    }

    @Test
    void abortRemovesCanaryImmediately() {
        controller.start("deploy-3", defaultPolicy);
        controller.abort("deploy-3");
        assertFalse(controller.isActive("deploy-3"));
    }

    @Test
    void stepOnUnknownDeploymentThrows() {
        assertThrows(IllegalArgumentException.class, () -> controller.step("unknown", 0.0));
    }

    @Test
    void policyValidatesNegativeInitialPercent() {
        assertThrows(IllegalArgumentException.class, () ->
                CanaryPolicy.builder("bad").initialTrafficPercent(-1).build());
    }

    @Test
    void policyErrorRateThresholdIsRespected() {
        assertTrue(defaultPolicy.isErrorRateAcceptable(0.04));
        assertFalse(defaultPolicy.isErrorRateAcceptable(0.06));
    }
}
