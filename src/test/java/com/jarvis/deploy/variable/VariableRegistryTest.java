package com.jarvis.deploy.variable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class VariableRegistryTest {

    private VariableRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new VariableRegistry();
    }

    @Test
    void shouldRegisterAndRetrieveVariable() {
        DeploymentVariable var = new DeploymentVariable("DB_URL", "jdbc:localhost", "staging", false);
        registry.register(var);

        Optional<DeploymentVariable> result = registry.get("DB_URL", "staging");
        assertTrue(result.isPresent());
        assertEquals("jdbc:localhost", result.get().getValue());
    }

    @Test
    void shouldReturnEmptyForUnknownVariable() {
        Optional<DeploymentVariable> result = registry.get("UNKNOWN", "prod");
        assertFalse(result.isPresent());
    }

    @Test
    void shouldUpdateExistingVariable() {
        registry.register(new DeploymentVariable("API_KEY", "old-key", "prod", true));
        boolean updated = registry.update("API_KEY", "prod", "new-key");

        assertTrue(updated);
        assertEquals("new-key", registry.get("API_KEY", "prod").get().getValue());
    }

    @Test
    void shouldReturnFalseWhenUpdatingNonExistentVariable() {
        boolean updated = registry.update("MISSING", "dev", "value");
        assertFalse(updated);
    }

    @Test
    void shouldRemoveVariable() {
        registry.register(new DeploymentVariable("TIMEOUT", "30", "dev", false));
        boolean removed = registry.remove("TIMEOUT", "dev");

        assertTrue(removed);
        assertFalse(registry.get("TIMEOUT", "dev").isPresent());
    }

    @Test
    void shouldListVariablesByEnvironment() {
        registry.register(new DeploymentVariable("HOST", "localhost", "dev", false));
        registry.register(new DeploymentVariable("PORT", "8080", "dev", false));
        registry.register(new DeploymentVariable("HOST", "prod-host", "prod", false));

        List<DeploymentVariable> devVars = registry.listByEnvironment("dev");
        assertEquals(2, devVars.size());
        assertTrue(devVars.stream().allMatch(v -> v.getEnvironment().equals("dev")));
    }

    @Test
    void shouldRedactSensitiveVariableInToString() {
        DeploymentVariable secret = new DeploymentVariable("SECRET", "my-secret", "prod", true);
        assertFalse(secret.toString().contains("my-secret"));
        assertTrue(secret.toString().contains("[REDACTED]"));
    }

    @Test
    void shouldThrowOnBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new DeploymentVariable("", "value", "dev", false));
    }

    @Test
    void shouldReportCorrectSize() {
        registry.register(new DeploymentVariable("A", "1", "dev", false));
        registry.register(new DeploymentVariable("B", "2", "dev", false));
        assertEquals(2, registry.size());
    }
}
