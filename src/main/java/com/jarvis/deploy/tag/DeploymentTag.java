package com.jarvis.deploy.tag;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a named tag applied to a deployment for categorization,
 * filtering, and metadata enrichment purposes.
 */
public class DeploymentTag {

    private final String name;
    private final String value;
    private final String environment;
    private final Instant createdAt;
    private final Map<String, String> metadata;

    public DeploymentTag(String name, String value, String environment) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tag name must not be null or blank");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Tag value must not be null or blank");
        }
        this.name = name.trim().toLowerCase();
        this.value = value.trim();
        this.environment = environment;
        this.createdAt = Instant.now();
        this.metadata = new HashMap<>();
    }

    public String getName() { return name; }
    public String getValue() { return value; }
    public String getEnvironment() { return environment; }
    public Instant getCreatedAt() { return createdAt; }

    public void addMetadata(String key, String val) {
        if (key != null && val != null) {
            metadata.put(key, val);
        }
    }

    public Map<String, String> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentTag)) return false;
        DeploymentTag that = (DeploymentTag) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, environment);
    }

    @Override
    public String toString() {
        return String.format("DeploymentTag{name='%s', value='%s', env='%s'}", name, value, environment);
    }
}
