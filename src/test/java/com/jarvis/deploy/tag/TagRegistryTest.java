package com.jarvis.deploy.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TagRegistryTest {

    private TagRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TagRegistry();
    }

    @Test
    void shouldRegisterAndFindTagByEnvironmentAndName() {
        DeploymentTag tag = new DeploymentTag("version", "1.4.2", "production");
        registry.register(tag);

        Optional<DeploymentTag> found = registry.find("production", "version");
        assertTrue(found.isPresent());
        assertEquals("1.4.2", found.get().getValue());
    }

    @Test
    void shouldReturnEmptyWhenTagNotFound() {
        Optional<DeploymentTag> found = registry.find("staging", "nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindAllTagsForEnvironment() {
        registry.register(new DeploymentTag("version", "2.0.0", "staging"));
        registry.register(new DeploymentTag("team", "backend", "staging"));
        registry.register(new DeploymentTag("version", "1.9.0", "production"));

        List<DeploymentTag> stagingTags = registry.findByEnvironment("staging");
        assertEquals(2, stagingTags.size());
    }

    @Test
    void shouldFindTagsByValue() {
        registry.register(new DeploymentTag("team", "backend", "staging"));
        registry.register(new DeploymentTag("owner", "backend", "production"));
        registry.register(new DeploymentTag("team", "frontend", "staging"));

        List<DeploymentTag> backendTags = registry.findByValue("backend");
        assertEquals(2, backendTags.size());
    }

    @Test
    void shouldRemoveTagSuccessfully() {
        registry.register(new DeploymentTag("version", "1.0.0", "dev"));
        assertTrue(registry.remove("dev", "version"));
        assertFalse(registry.find("dev", "version").isPresent());
    }

    @Test
    void shouldReturnFalseWhenRemovingNonExistentTag() {
        assertFalse(registry.remove("dev", "missing"));
    }

    @Test
    void shouldOverwriteTagWithSameNameAndEnvironment() {
        registry.register(new DeploymentTag("version", "1.0.0", "staging"));
        registry.register(new DeploymentTag("version", "2.0.0", "staging"));

        assertEquals(1, registry.size());
        assertEquals("2.0.0", registry.find("staging", "version").get().getValue());
    }

    @Test
    void shouldThrowOnNullTagName() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentTag(null, "value", "prod"));
    }

    @Test
    void shouldThrowOnBlankTagValue() {
        assertThrows(IllegalArgumentException.class, () -> new DeploymentTag("key", "  ", "prod"));
    }

    @Test
    void shouldSupportMetadataOnTag() {
        DeploymentTag tag = new DeploymentTag("release", "hotfix", "production");
        tag.addMetadata("jira", "PROJ-123");
        tag.addMetadata("approved-by", "alice");

        assertEquals("PROJ-123", tag.getMetadata().get("jira"));
        assertEquals(2, tag.getMetadata().size());
    }
}
