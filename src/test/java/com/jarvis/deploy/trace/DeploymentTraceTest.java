package com.jarvis.deploy.trace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentTraceTest {

    private DeploymentTrace trace;

    @BeforeEach
    void setUp() {
        trace = new DeploymentTrace("deploy-001", "production");
    }

    @Test
    void shouldAssignUniqueTraceId() {
        DeploymentTrace other = new DeploymentTrace("deploy-002", "staging");
        assertNotEquals(trace.getTraceId(), other.getTraceId());
    }

    @Test
    void shouldReturnCorrectDeploymentAndEnvironment() {
        assertEquals("deploy-001", trace.getDeploymentId());
        assertEquals("production", trace.getEnvironment());
    }

    @Test
    void shouldNotBeCompletedInitially() {
        assertFalse(trace.isCompleted());
        assertNull(trace.getCompletedAt());
    }

    @Test
    void shouldMarkAsCompletedAndRecordTimestamp() throws InterruptedException {
        Thread.sleep(5);
        trace.complete();
        assertTrue(trace.isCompleted());
        assertNotNull(trace.getCompletedAt());
        assertTrue(trace.durationMillis() >= 0);
    }

    @Test
    void shouldAddAndRetrieveSpans() {
        TraceSpan span = new TraceSpan(trace.getTraceId(), "health-check");
        span.finish();
        trace.addSpan(span);
        assertEquals(1, trace.getSpans().size());
        assertEquals("health-check", trace.getSpans().get(0).getOperationName());
    }

    @Test
    void shouldRejectNullSpan() {
        assertThrows(IllegalArgumentException.class, () -> trace.addSpan(null));
    }

    @Test
    void shouldReturnUnmodifiableSpanList() {
        assertThrows(UnsupportedOperationException.class,
                () -> trace.getSpans().add(new TraceSpan(trace.getTraceId(), "op")));
    }

    @Test
    void spanShouldCaptureErrorDetails() {
        TraceSpan span = new TraceSpan(trace.getTraceId(), "deploy-artifact");
        span.tag("artifact", "app-1.2.3.jar");
        span.finishWithError("Connection refused");
        assertTrue(span.isError());
        assertEquals("Connection refused", span.getErrorMessage());
        assertEquals("app-1.2.3.jar", span.getTags().get("artifact"));
        assertTrue(span.durationMillis() >= 0);
    }

    @Test
    void spanShouldRejectBlankOperationName() {
        assertThrows(IllegalArgumentException.class,
                () -> new TraceSpan(trace.getTraceId(), "  "));
    }

    @Test
    void toStringShouldContainKeyFields() {
        String str = trace.toString();
        assertTrue(str.contains("deploy-001"));
        assertTrue(str.contains("production"));
    }
}
