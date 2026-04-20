package com.jarvis.deploy.circuit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CircuitBreakerRegistryTest {

    private CircuitBreakerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CircuitBreakerRegistry(3, 5000L);
    }

    @Test
    void getOrCreateReturnsSameInstance() {
        CircuitBreaker first = registry.getOrCreate("svc-a");
        CircuitBreaker second = registry.getOrCreate("svc-a");
        assertSame(first, second);
    }

    @Test
    void getOrCreateCreatesWithDefaults() {
        CircuitBreaker cb = registry.getOrCreate("svc-b");
        assertEquals("svc-b", cb.getName());
        assertEquals(CircuitState.CLOSED, cb.getState());
    }

    @Test
    void registerOverwritesExistingEntry() {
        registry.getOrCreate("svc-c");
        CircuitBreaker custom = new CircuitBreaker("svc-c", 10, 1000L);
        registry.register(custom);
        assertSame(custom, registry.find("svc-c").orElseThrow());
    }

    @Test
    void findReturnsEmptyForUnknownName() {
        Optional<CircuitBreaker> result = registry.find("unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void removeDeletesEntry() {
        registry.getOrCreate("svc-d");
        assertTrue(registry.remove("svc-d"));
        assertTrue(registry.find("svc-d").isEmpty());
    }

    @Test
    void removeReturnsFalseForMissingEntry() {
        assertFalse(registry.remove("nonexistent"));
    }

    @Test
    void resetAllClosesAllBreakers() {
        CircuitBreaker cb1 = registry.getOrCreate("svc-e");
        CircuitBreaker cb2 = registry.getOrCreate("svc-f");
        for (int i = 0; i < 3; i++) { cb1.recordFailure(); cb2.recordFailure(); }
        assertEquals(CircuitState.OPEN, cb1.getState());
        assertEquals(CircuitState.OPEN, cb2.getState());

        registry.resetAll();

        assertEquals(CircuitState.CLOSED, cb1.getState());
        assertEquals(CircuitState.CLOSED, cb2.getState());
    }

    @Test
    void sizeReflectsRegisteredBreakers() {
        assertEquals(0, registry.size());
        registry.getOrCreate("svc-g");
        registry.getOrCreate("svc-h");
        assertEquals(2, registry.size());
    }

    @Test
    void allReturnsUnmodifiableView() {
        registry.getOrCreate("svc-i");
        assertThrows(UnsupportedOperationException.class,
                () -> registry.all().put("x", new CircuitBreaker("x", 1, 1000L)));
    }
}
