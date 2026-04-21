package com.jarvis.deploy.manifest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentManifestTest {

    private DeploymentManifest manifest;

    @BeforeEach
    void setUp() {
        manifest = new DeploymentManifest("manifest-001", "payment-service", "2.4.1", "production");
    }

    @Test
    void shouldCreateManifestWithRequiredFields() {
        assertEquals("manifest-001", manifest.getManifestId());
        assertEquals("payment-service", manifest.getApplicationName());
        assertEquals("2.4.1", manifest.getVersion());
        assertEquals("production", manifest.getEnvironment());
        assertNotNull(manifest.getCreatedAt());
    }

    @Test
    void shouldAddAndRetrieveProperty() {
        manifest.addProperty("replicas", "3");
        assertEquals("3", manifest.getProperty("replicas"));
        assertTrue(manifest.hasProperty("replicas"));
    }

    @Test
    void shouldReturnNullForMissingProperty() {
        assertNull(manifest.getProperty("nonexistent"));
        assertFalse(manifest.hasProperty("nonexistent"));
    }

    @Test
    void shouldReturnUnmodifiableProperties() {
        manifest.addProperty("key", "value");
        assertThrows(UnsupportedOperationException.class,
                () -> manifest.getProperties().put("extra", "blocked"));
    }

    @Test
    void shouldSetAndGetChecksum() {
        assertNull(manifest.getChecksum());
        manifest.setChecksum("sha256:abcdef1234567890");
        assertEquals("sha256:abcdef1234567890", manifest.getChecksum());
    }

    @Test
    void shouldThrowOnNullManifestId() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentManifest(null, "app", "1.0", "dev"));
    }

    @Test
    void shouldThrowOnNullApplicationName() {
        assertThrows(NullPointerException.class,
                () -> new DeploymentManifest("id", null, "1.0", "dev"));
    }

    @Test
    void shouldThrowOnNullPropertyKey() {
        assertThrows(NullPointerException.class, () -> manifest.addProperty(null, "value"));
    }

    @Test
    void shouldSupportMultipleProperties() {
        manifest.addProperty("cpu", "500m");
        manifest.addProperty("memory", "256Mi");
        manifest.addProperty("port", "8080");
        assertEquals(3, manifest.getProperties().size());
    }

    @Test
    void shouldOverwriteExistingProperty() {
        manifest.addProperty("replicas", "2");
        manifest.addProperty("replicas", "5");
        assertEquals("5", manifest.getProperty("replicas"));
    }

    @Test
    void shouldHaveMeaningfulToString() {
        String result = manifest.toString();
        assertTrue(result.contains("manifest-001"));
        assertTrue(result.contains("payment-service"));
        assertTrue(result.contains("2.4.1"));
        assertTrue(result.contains("production"));
    }
}
