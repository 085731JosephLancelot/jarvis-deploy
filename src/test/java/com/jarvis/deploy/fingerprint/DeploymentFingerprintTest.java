package com.jarvis.deploy.fingerprint;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentFingerprintTest {

    private Map<String, String> sampleConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("replicas", "3");
        config.put("memory", "512m");
        config.put("cpu", "0.5");
        return config;
    }

    @Test
    void shouldComputeNonNullHash() {
        DeploymentFingerprint fp = new DeploymentFingerprint("dep-1", "staging", "v1.2.3", sampleConfig());
        assertNotNull(fp.getHash());
        assertFalse(fp.getHash().isEmpty());
    }

    @Test
    void shouldProduceSameHashForIdenticalInputs() {
        DeploymentFingerprint fp1 = new DeploymentFingerprint("dep-1", "staging", "v1.2.3", sampleConfig());
        DeploymentFingerprint fp2 = new DeploymentFingerprint("dep-1", "staging", "v1.2.3", sampleConfig());
        assertEquals(fp1.getHash(), fp2.getHash());
        assertTrue(fp1.matches(fp2));
    }

    @Test
    void shouldProduceDifferentHashWhenVersionChanges() {
        DeploymentFingerprint fp1 = new DeploymentFingerprint("dep-1", "staging", "v1.2.3", sampleConfig());
        DeploymentFingerprint fp2 = new DeploymentFingerprint("dep-1", "staging", "v1.2.4", sampleConfig());
        assertNotEquals(fp1.getHash(), fp2.getHash());
        assertFalse(fp1.matches(fp2));
    }

    @Test
    void shouldProduceDifferentHashWhenConfigChanges() {
        Map<String, String> altConfig = sampleConfig();
        altConfig.put("replicas", "5");
        DeploymentFingerprint fp1 = new DeploymentFingerprint("dep-1", "prod", "v2.0.0", sampleConfig());
        DeploymentFingerprint fp2 = new DeploymentFingerprint("dep-1", "prod", "v2.0.0", altConfig);
        assertNotEquals(fp1.getHash(), fp2.getHash());
    }

    @Test
    void shouldProduceDifferentHashWhenEnvironmentChanges() {
        DeploymentFingerprint fp1 = new DeploymentFingerprint("dep-1", "staging", "v1.0.0", sampleConfig());
        DeploymentFingerprint fp2 = new DeploymentFingerprint("dep-1", "prod", "v1.0.0", sampleConfig());
        assertFalse(fp1.matches(fp2));
    }

    @Test
    void shouldReturnFalseWhenMatchingAgainstNull() {
        DeploymentFingerprint fp = new DeploymentFingerprint("dep-1", "staging", "v1.0.0", sampleConfig());
        assertFalse(fp.matches(null));
    }

    @Test
    void shouldThrowOnNullDeploymentId() {
        assertThrows(NullPointerException.class, () ->
                new DeploymentFingerprint(null, "staging", "v1.0.0", sampleConfig()));
    }

    @Test
    void shouldExposeFieldsCorrectly() {
        DeploymentFingerprint fp = new DeploymentFingerprint("dep-42", "prod", "v3.1.0", sampleConfig());
        assertEquals("dep-42", fp.getDeploymentId());
        assertEquals("prod", fp.getEnvironment());
        assertEquals("v3.1.0", fp.getArtifactVersion());
        assertNotNull(fp.getComputedAt());
        assertEquals(3, fp.getConfigEntries().size());
    }

    @Test
    void shouldReturnImmutableConfigEntries() {
        DeploymentFingerprint fp = new DeploymentFingerprint("dep-1", "staging", "v1.0.0", sampleConfig());
        assertThrows(UnsupportedOperationException.class, () -> fp.getConfigEntries().put("new", "value"));
    }
}
