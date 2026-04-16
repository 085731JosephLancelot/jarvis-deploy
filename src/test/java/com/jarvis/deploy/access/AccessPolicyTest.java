package com.jarvis.deploy.access;

import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import static org.junit.jupiter.api.Assertions.*;

class AccessPolicyTest {

    @Test
    void testCreation() {
        AccessPolicy p = new AccessPolicy("alice", "prod", EnumSet.of(AccessRole.DEPLOYER));
        assertEquals("alice", p.getPrincipal());
        assertEquals("prod", p.getEnvironment());
        assertTrue(p.hasRole(AccessRole.DEPLOYER));
        assertFalse(p.hasRole(AccessRole.ADMIN));
    }

    @Test
    void testRolesImmutable() {
        AccessPolicy p = new AccessPolicy("bob", "staging", EnumSet.of(AccessRole.VIEWER));
        assertThrows(UnsupportedOperationException.class, () -> p.getRoles().add(AccessRole.ADMIN));
    }

    @Test
    void testBlankPrincipalThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new AccessPolicy("", "prod", EnumSet.of(AccessRole.VIEWER)));
    }

    @Test
    void testBlankEnvironmentThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> new AccessPolicy("alice", "  ", EnumSet.of(AccessRole.VIEWER)));
    }

    @Test
    void testToString() {
        AccessPolicy p = new AccessPolicy("carol", "dev", EnumSet.of(AccessRole.APPROVER));
        assertTrue(p.toString().contains("carol"));
        assertTrue(p.toString().contains("dev"));
    }
}
