package com.jarvis.deploy.access;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AccessControlManagerTest {

    private AccessControlManager manager;

    @BeforeEach
    void setUp() {
        manager = new AccessControlManager();
    }

    @Test
    void testGrantAndCheck() {
        manager.grant(new AccessPolicy("alice", "prod", EnumSet.of(AccessRole.DEPLOYER)));
        assertTrue(manager.isAllowed("alice", "prod", AccessRole.DEPLOYER));
        assertFalse(manager.isAllowed("alice", "prod", AccessRole.ADMIN));
    }

    @Test
    void testRevoke() {
        manager.grant(new AccessPolicy("bob", "staging", EnumSet.of(AccessRole.VIEWER)));
        manager.revoke("bob", "staging");
        assertFalse(manager.isAllowed("bob", "staging", AccessRole.VIEWER));
    }

    @Test
    void testUnknownPrincipalDenied() {
        assertFalse(manager.isAllowed("unknown", "prod", AccessRole.VIEWER));
    }

    @Test
    void testGetPolicy() {
        manager.grant(new AccessPolicy("carol", "dev", EnumSet.of(AccessRole.APPROVER)));
        assertTrue(manager.getPolicy("carol", "dev").isPresent());
        assertFalse(manager.getPolicy("carol", "prod").isPresent());
    }

    @Test
    void testGetPoliciesForEnvironment() {
        manager.grant(new AccessPolicy("alice", "prod", EnumSet.of(AccessRole.DEPLOYER)));
        manager.grant(new AccessPolicy("bob", "prod", EnumSet.of(AccessRole.VIEWER)));
        manager.grant(new AccessPolicy("carol", "dev", EnumSet.of(AccessRole.ADMIN)));
        List<AccessPolicy> prodPolicies = manager.getPoliciesForEnvironment("prod");
        assertEquals(2, prodPolicies.size());
    }

    @Test
    void testGetPoliciesForPrincipal() {
        manager.grant(new AccessPolicy("alice", "prod", EnumSet.of(AccessRole.DEPLOYER)));
        manager.grant(new AccessPolicy("alice", "dev", EnumSet.of(AccessRole.ADMIN)));
        List<AccessPolicy> alicePolicies = manager.getPoliciesForPrincipal("alice");
        assertEquals(2, alicePolicies.size());
    }

    @Test
    void testSize() {
        assertEquals(0, manager.size());
        manager.grant(new AccessPolicy("alice", "prod", EnumSet.of(AccessRole.DEPLOYER)));
        assertEquals(1, manager.size());
    }
}
