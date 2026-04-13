package com.jarvis.deploy.approval;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalGateTest {

    private ApprovalGate gate;

    @BeforeEach
    void setUp() {
        gate = new ApprovalGate("production", "alice");
    }

    @Test
    void newGateShouldBePending() {
        assertEquals(ApprovalStatus.PENDING, gate.getStatus());
        assertFalse(gate.isApproved());
        assertNotNull(gate.getId());
        assertNotNull(gate.getRequestedAt());
    }

    @Test
    void approveShouldTransitionToApproved() {
        gate.approve("bob", "Looks good");

        assertEquals(ApprovalStatus.APPROVED, gate.getStatus());
        assertTrue(gate.isApproved());
        assertEquals("bob", gate.getApprovedBy());
        assertEquals("Looks good", gate.getComment());
        assertNotNull(gate.getResolvedAt());
    }

    @Test
    void rejectShouldTransitionToRejected() {
        gate.reject("carol", "Not ready");

        assertEquals(ApprovalStatus.REJECTED, gate.getStatus());
        assertFalse(gate.isApproved());
        assertEquals("carol", gate.getApprovedBy());
        assertEquals("Not ready", gate.getComment());
        assertNotNull(gate.getResolvedAt());
    }

    @Test
    void approvingAlreadyApprovedGateShouldThrow() {
        gate.approve("bob", "ok");
        assertThrows(IllegalStateException.class, () -> gate.approve("dave", "again"));
    }

    @Test
    void rejectingAlreadyRejectedGateShouldThrow() {
        gate.reject("bob", "no");
        assertThrows(IllegalStateException.class, () -> gate.reject("dave", "still no"));
    }

    @Test
    void approvingRejectedGateShouldThrow() {
        gate.reject("bob", "no");
        assertThrows(IllegalStateException.class, () -> gate.approve("dave", "override"));
    }

    @Test
    void nullEnvironmentShouldThrow() {
        assertThrows(NullPointerException.class, () -> new ApprovalGate(null, "alice"));
    }

    @Test
    void nullRequesterShouldThrow() {
        assertThrows(NullPointerException.class, () -> new ApprovalGate("staging", null));
    }

    @Test
    void nullApproverShouldThrow() {
        assertThrows(NullPointerException.class, () -> gate.approve(null, "comment"));
    }

    @Test
    void toStringShouldContainKeyFields() {
        String str = gate.toString();
        assertTrue(str.contains("production"));
        assertTrue(str.contains("alice"));
        assertTrue(str.contains("PENDING"));
    }
}
