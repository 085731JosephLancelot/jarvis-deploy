package com.jarvis.deploy.config;

import com.jarvis.deploy.environment.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentConfigTest {

    private EnvironmentConfig config;
    private Environment devEnv;
    private Environment prodEnv;

    @BeforeEach
    void setUp() {
        config = new EnvironmentConfig();
        devEnv = new Environment("dev", "http://dev.internal", false);
        prodEnv = new Environment("prod", "http://prod.internal", true);
    }

    @Test
    void register_shouldAddEnvironmentSuccessfully() {
        config.register(devEnv);
        assertEquals(1, config.size());
        assertTrue(config.contains("dev"));
    }

    @Test
    void register_shouldThrowOnNullEnvironment() {
        assertThrows(IllegalArgumentException.class, () -> config.register(null));
    }

    @Test
    void register_shouldThrowOnDuplicateEnvironment() {
        config.register(devEnv);
        Environment duplicate = new Environment("dev", "http://dev2.internal", false);
        assertThrows(IllegalArgumentException.class, () -> config.register(duplicate));
    }

    @Test
    void get_shouldReturnEnvironmentByName() {
        config.register(devEnv);
        Optional<Environment> result = config.get("dev");
        assertTrue(result.isPresent());
        assertEquals("dev", result.get().getName());
    }

    @Test
    void get_shouldReturnEmptyForUnknownName() {
        Optional<Environment> result = config.get("staging");
        assertFalse(result.isPresent());
    }

    @Test
    void get_shouldReturnEmptyForNullOrBlankName() {
        assertFalse(config.get(null).isPresent());
        assertFalse(config.get("  ").isPresent());
    }

    @Test
    void listEnvironments_shouldReturnAllRegisteredNames() {
        config.register(devEnv);
        config.register(prodEnv);
        Set<String> names = config.listEnvironments();
        assertEquals(2, names.size());
        assertTrue(names.contains("dev"));
        assertTrue(names.contains("prod"));
    }

    @Test
    void deregister_shouldRemoveEnvironment() {
        config.register(devEnv);
        boolean removed = config.deregister("dev");
        assertTrue(removed);
        assertFalse(config.contains("dev"));
        assertEquals(0, config.size());
    }

    @Test
    void deregister_shouldReturnFalseIfNotFound() {
        boolean removed = config.deregister("nonexistent");
        assertFalse(removed);
    }
}
