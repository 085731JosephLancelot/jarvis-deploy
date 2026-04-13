package com.jarvis.deploy.promotion;

import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PromotionServiceTest {

    private PromotionService promotionService;
    private AuditLogger auditLogger;
    private Deployment deployment;

    @BeforeEach
    void setUp() {
        auditLogger = mock(AuditLogger.class);
        promotionService = new PromotionService(auditLogger);
        deployment = mock(Deployment.class);
        when(deployment.getId()).thenReturn("deploy-42");
        when(deployment.getEnvironment()).thenReturn("staging");
    }

    @Test
    void registerPolicy_success() {
        PromotionPolicy policy = new PromotionPolicy("p1", "staging", "production", false, true, Set.of());
        promotionService.registerPolicy(policy);
        assertEquals(1, promotionService.getPolicies().size());
    }

    @Test
    void registerPolicy_duplicateIdThrows() {
        PromotionPolicy policy = new PromotionPolicy("p1", "staging", "production", false, false, Set.of());
        promotionService.registerPolicy(policy);
        PromotionPolicy duplicate = new PromotionPolicy("p1", "dev", "staging", false, false, Set.of());
        assertThrows(IllegalStateException.class, () -> promotionService.registerPolicy(duplicate));
    }

    @Test
    void findPolicy_returnsMatchingPolicy() {
        PromotionPolicy policy = new PromotionPolicy("p1", "staging", "production", false, false, Set.of());
        promotionService.registerPolicy(policy);
        Optional<PromotionPolicy> found = promotionService.findPolicy("staging", "production");
        assertTrue(found.isPresent());
        assertEquals("p1", found.get().getId());
    }

    @Test
    void findPolicy_returnsEmptyWhenNoMatch() {
        Optional<PromotionPolicy> found = promotionService.findPolicy("dev", "production");
        assertTrue(found.isEmpty());
    }

    @Test
    void promote_successWhenNoPolicyFound_returnsDenied() {
        PromotionResult result = promotion", "alice");
        assertEquals(PromotionResult.Status.DENIED, result.getStatus());
        verify(auditLogger).log(any());
    }

_pendingApprovalWhenPolicyRequiresApproval() {
        PromotionPolicy policy = new PromotionPolicy("p1", "staging", "production", true, false, Set.of());
        promotionService.registerPolicy(policy);
        PromotionResult result = promotionService.promote(deployment, "production", "alice");
        assertEquals(PromotionResult.Status.PENDING_APPROVAL, result.getStatus());
    }

    @Test
    void promote_successWhenPolicyDoesNotRequireApproval() {
        PromotionPolicy policy = new PromotionPolicy("p1", "staging", "production", false, false, Set.of());
        promotionService.registerPolicy(policy);
        PromotionResult result = promotionService.promote(deployment, "production", "alice");
        assertEquals(PromotionResult.Status.SUCCESS, result.getStatus());
        assertEquals("production", result.getTargetEnvironment());
    }

    @Test
    void promote_nullDeploymentThrows() {
        assertThrows(IllegalArgumentException.class, () -> promotionService.promote(null, "production", "alice"));
    }
}
