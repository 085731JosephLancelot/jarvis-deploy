package com.jarvis.deploy.environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a deployment environment (e.g., dev, staging, production).
 */
public class Environment {

    public enum Stage {
        DEV, STAGING, PRODUCTION
    }

    private final String name;
    private final Stage stage;
    private final String baseUrl;
    private final Map<String, String> properties;

    public Environment(String name, Stage stage, String baseUrl, Map<String, String> properties) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Environment name must not be blank");
        }
        this.name = name;
        this.stage = Objects.requireNonNull(stage, "Stage must not be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL must not be null");
        this.properties = Collections.unmodifiableMap(new HashMap<>(properties != null ? properties : Collections.emptyMap()));
    }

    public String getName() {
        return name;
    }

    public Stage getStage() {
        return stage;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public boolean isProduction() {
        return stage == Stage.PRODUCTION;
    }

    @Override
    public String toString() {
        return String.format("Environment{name='%s', stage=%s, baseUrl='%s'}", name, stage, baseUrl);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Environment)) return false;
        Environment that = (Environment) o;
        return Objects.equals(name, that.name) && stage == that.stage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, stage);
    }
}
