package com.jarvis.deploy.environment;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvironmentTest {

    @Test
    void shouldCreateEnvironmentWithValidFields() {
        Map<String, String> props = new HashMap<>();
        props.put("timeout", "30s");

        Environment env = new Environment("staging", Environment.Stage.STAGING, "https://staging.example.com", props);

        assertEquals("staging", env.getName());
        assertEquals(Environment.Stage.STAGING, env.getStage());
        assertEquals("https://staging.example.com", env.getBaseUrl());
        assertEquals("30s", env.getProperty("timeout"));
    }

    @Test
    void shouldReturnTrueForProductionStage() {
        Environment env = new Environment("prod", Environment.Stage.PRODUCTION, "https://prod.example.com", null);
        assertTrue(env.isProduction());
    }

    @Test
    void shouldReturnFalseForNonProductionStage() {
        Environment env = new Environment("dev", Environment.Stage.DEV, "http://localhost:8080", null);
        assertFalse(env.isProduction());
    }

    @Test
    void shouldThrowExceptionForBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
            new Environment("  ", Environment.Stage.DEV, "http://localhost", null)
        );
    }

    @Test
    void shouldThrowExceptionForNullStage() {
        assertThrows(NullPointerException.class, () ->
            new Environment("dev", null, "http://localhost", null)
        );
    }

    @Test
    void shouldReturnNullForMissingProperty() {
        Environment env = new Environment("dev", Environment.Stage.DEV, "http://localhost", null);
        assertNull(env.getProperty("nonexistent"));
    }

    @Test
    void propertiesMapShouldBeImmutable() {
        Map<String, String> props = new HashMap<>();
        props.put("key", "value");
        Environment env = new Environment("dev", Environment.Stage.DEV, "http://localhost", props);

        assertThrows(UnsupportedOperationException.class, () ->
            env.getProperties().put("newKey", "newValue")
        );
    }

    @Test
    void equalEnvironmentsShouldHaveSameHashCode() {
        Environment e1 = new Environment("prod", Environment.Stage.PRODUCTION, "https://prod.example.com", null);
        Environment e2 = new Environment("prod", Environment.Stage.PRODUCTION, "https://other.example.com", null);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void toStringShouldContainNameAndStage() {
        Environment env = new Environment("staging", Environment.Stage.STAGING, "https://staging.example.com", null);
        String result = env.toString();
        assertTrue(result.contains("staging"));
        assertTrue(result.contains("STAGING"));
    }
}
