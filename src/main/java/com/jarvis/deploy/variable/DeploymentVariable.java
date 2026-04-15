package com.jarvis.deploy.variable;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a named variable scoped to a deployment environment.
 */
public class DeploymentVariable {

    private final String name;
    private String value;
    private final String environment;
    private final boolean sensitive;
    private Instant lastUpdated;

    public DeploymentVariable(String name, String value, String environment, boolean sensitive) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Variable name must not be blank");
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("Environment must not be blank");
        this.name = name;
        this.value = value;
        this.environment = environment;
        this.sensitive = sensitive;
        this.lastUpdated = Instant.now();
    }

    public String getName() { return name; }

    public String getValue() { return value; }

    public String getEnvironment() { return environment; }

    public boolean isSensitive() { return sensitive; }

    public Instant getLastUpdated() { return lastUpdated; }

    public void setValue(String value) {
        this.value = value;
        this.lastUpdated = Instant.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeploymentVariable)) return false;
        DeploymentVariable that = (DeploymentVariable) o;
        return Objects.equals(name, that.name) && Objects.equals(environment, that.environment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, environment);
    }

    @Override
    public String toString() {
        String displayValue = sensitive ? "[REDACTED]" : value;
        return "DeploymentVariable{name='" + name + "', environment='" + environment + "', value='" + displayValue + "', sensitive=" + sensitive + "}";
    }
}
