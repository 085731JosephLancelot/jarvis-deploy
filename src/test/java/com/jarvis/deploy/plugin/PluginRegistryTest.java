package com.jarvis.deploy.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PluginRegistryTest {

    private PluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new PluginRegistry();
    }

    private DeploymentPlugin plugin(String id, PluginType type) {
        return new DeploymentPlugin(id, "Plugin " + id, "1.0.0", type, Map.of("key", "value"));
    }

    @Test
    void shouldRegisterAndFindPlugin() {
        DeploymentPlugin p = plugin("p1", PluginType.PRE_DEPLOY);
        registry.register(p);
        assertTrue(registry.findById("p1").isPresent());
        assertEquals("p1", registry.findById("p1").get().getId());
    }

    @Test
    void shouldThrowOnDuplicateRegistration() {
        registry.register(plugin("p1", PluginType.PRE_DEPLOY));
        assertThrows(IllegalStateException.class, () -> registry.register(plugin("p1", PluginType.POST_DEPLOY)));
    }

    @Test
    void shouldUnregisterPlugin() {
        registry.register(plugin("p1", PluginType.PRE_DEPLOY));
        registry.unregister("p1");
        assertFalse(registry.findById("p1").isPresent());
    }

    @Test
    void shouldThrowWhenUnregisteringNonExistent() {
        assertThrows(NoSuchElementException.class, () -> registry.unregister("ghost"));
    }

    @Test
    void shouldFindByType() {
        registry.register(plugin("p1", PluginType.PRE_DEPLOY));
        registry.register(plugin("p2", PluginType.PRE_DEPLOY));
        registry.register(plugin("p3", PluginType.POST_DEPLOY));
        List<DeploymentPlugin> prePlugins = registry.findByType(PluginType.PRE_DEPLOY);
        assertEquals(2, prePlugins.size());
    }

    @Test
    void shouldFindOnlyEnabledPlugins() {
        DeploymentPlugin p1 = plugin("p1", PluginType.NOTIFICATION);
        DeploymentPlugin p2 = plugin("p2", PluginType.NOTIFICATION);
        p2.disable();
        registry.register(p1);
        registry.register(p2);
        List<DeploymentPlugin> enabled = registry.findEnabled();
        assertEquals(1, enabled.size());
        assertEquals("p1", enabled.get(0).getId());
    }

    @Test
    void shouldFindEnabledByType() {
        DeploymentPlugin p1 = plugin("p1", PluginType.HEALTH_CHECK);
        DeploymentPlugin p2 = plugin("p2", PluginType.HEALTH_CHECK);
        p1.disable();
        registry.register(p1);
        registry.register(p2);
        List<DeploymentPlugin> result = registry.findEnabledByType(PluginType.HEALTH_CHECK);
        assertEquals(1, result.size());
        assertEquals("p2", result.get(0).getId());
    }

    @Test
    void shouldReportCorrectSize() {
        assertEquals(0, registry.size());
        registry.register(plugin("p1", PluginType.PRE_ROLLBACK));
        registry.register(plugin("p2", PluginType.POST_ROLLBACK));
        assertEquals(2, registry.size());
    }

    @Test
    void shouldReturnEmptyOptionalForMissingPlugin() {
        assertTrue(registry.findById("missing").isEmpty());
    }
}
