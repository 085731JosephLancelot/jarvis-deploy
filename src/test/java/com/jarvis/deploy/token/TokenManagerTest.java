package com.jarvis.deploy.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TokenManagerTest {

    private TokenManager tokenManager;

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager(300L);
    }

    @Test
    void issueCreatesValidToken() {
        DeploymentToken token = tokenManager.issue("staging", "alice");
        assertNotNull(token);
        assertNotNull(token.getTokenId());
        assertEquals("staging", token.getEnvironment());
        assertEquals("alice", token.getIssuedBy());
        assertTrue(token.isValid());
    }

    @Test
    void issueWithCustomTtl() {
        DeploymentToken token = tokenManager.issue("prod", "bob", 600L);
        assertTrue(token.isValid());
        assertTrue(token.getExpiresAt().isAfter(token.getIssuedAt()));
    }

    @Test
    void findReturnsTokenById() {
        DeploymentToken token = tokenManager.issue("dev", "carol");
        Optional<DeploymentToken> found = tokenManager.find(token.getTokenId());
        assertTrue(found.isPresent());
        assertEquals(token.getTokenId(), found.get().getTokenId());
    }

    @Test
    void findReturnsEmptyForUnknownId() {
        Optional<DeploymentToken> found = tokenManager.find("nonexistent-id");
        assertFalse(found.isPresent());
    }

    @Test
    void validateReturnsTrueForValidToken() {
        DeploymentToken token = tokenManager.issue("staging", "dave");
        assertTrue(tokenManager.validate(token.getTokenId()));
    }

    @Test
    void validateReturnsFalseForRevokedToken() {
        DeploymentToken token = tokenManager.issue("staging", "eve");
        tokenManager.revoke(token.getTokenId());
        assertFalse(tokenManager.validate(token.getTokenId()));
    }

    @Test
    void validateReturnsFalseForUnknownToken() {
        assertFalse(tokenManager.validate("unknown"));
    }

    @Test
    void revokeReturnsFalseForMissingToken() {
        assertFalse(tokenManager.revoke("ghost-token"));
    }

    @Test
    void listActiveExcludesRevokedTokens() {
        DeploymentToken t1 = tokenManager.issue("dev", "frank");
        DeploymentToken t2 = tokenManager.issue("prod", "grace");
        tokenManager.revoke(t1.getTokenId());
        Collection<DeploymentToken> active = tokenManager.listActive();
        assertFalse(active.stream().anyMatch(t -> t.getTokenId().equals(t1.getTokenId())));
        assertTrue(active.stream().anyMatch(t -> t.getTokenId().equals(t2.getTokenId())));
    }

    @Test
    void purgeExpiredRemovesInvalidEntries() {
        tokenManager.issue("dev", "henry");
        DeploymentToken expired = tokenManager.issue("staging", "ivy", 1L);
        expired.revoke();
        int purged = tokenManager.purgeExpired();
        assertTrue(purged >= 1);
    }

    @Test
    void constructorRejectsNonPositiveTtl() {
        assertThrows(IllegalArgumentException.class, () -> new TokenManager(0));
        assertThrows(IllegalArgumentException.class, () -> new TokenManager(-5));
    }
}
