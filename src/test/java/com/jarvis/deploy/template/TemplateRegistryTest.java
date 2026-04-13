package com.jarvis.deploy.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TemplateRegistryTest {

    private TemplateRegistry registry;

    private DeploymentTemplate buildTemplate(String id, String name, String version,
                                              Map<String, String> params) {
        return new DeploymentTemplate(id, name, "desc", params, version);
    }

    @BeforeEach
    void setUp() {
        registry = new TemplateRegistry();
    }

    @Test
    void registerAndFindById_returnsTemplate() {
        DeploymentTemplate t = buildTemplate("tpl-1", "Basic Deploy", "1.0",
                Map.of("replicas", "2"));
        registry.register(t);

        Optional<DeploymentTemplate> found = registry.findById("tpl-1");
        assertTrue(found.isPresent());
        assertEquals("Basic Deploy", found.get().getName());
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        Optional<DeploymentTemplate> found = registry.findById("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void findById_nullId_returnsEmpty() {
        assertFalse(registry.findById(null).isPresent());
    }

    @Test
    void register_null_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> registry.register(null));
    }

    @Test
    void register_overwrites_existingTemplate() {
        registry.register(buildTemplate("tpl-1", "Old", "1.0", Map.of()));
        registry.register(buildTemplate("tpl-1", "New", "2.0", Map.of()));

        assertEquals("New", registry.findById("tpl-1").get().getName());
        assertEquals(1, registry.size());
    }

    @Test
    void listAll_returnsAllTemplates() {
        registry.register(buildTemplate("tpl-1", "A", "1.0", Map.of()));
        registry.register(buildTemplate("tpl-2", "B", "1.0", Map.of()));

        List<DeploymentTemplate> all = registry.listAll();
        assertEquals(2, all.size());
    }

    @Test
    void remove_existingTemplate_returnsTrue() {
        registry.register(buildTemplate("tpl-1", "A", "1.0", Map.of()));
        assertTrue(registry.remove("tpl-1"));
        assertFalse(registry.contains("tpl-1"));
    }

    @Test
    void remove_nonexistentTemplate_returnsFalse() {
        assertFalse(registry.remove("ghost"));
    }

    @Test
    void resolveParameters_mergesOverrides() {
        DeploymentTemplate t = buildTemplate("tpl-1", "A", "1.0",
                Map.of("replicas", "1", "timeout", "30"));
        Map<String, String> resolved = t.resolveParameters(Map.of("replicas", "5"));

        assertEquals("5", resolved.get("replicas"));
        assertEquals("30", resolved.get("timeout"));
    }

    @Test
    void resolveParameters_nullOverrides_returnsDefaults() {
        DeploymentTemplate t = buildTemplate("tpl-1", "A", "1.0",
                Map.of("replicas", "3"));
        Map<String, String> resolved = t.resolveParameters(null);
        assertEquals("3", resolved.get("replicas"));
    }
}
