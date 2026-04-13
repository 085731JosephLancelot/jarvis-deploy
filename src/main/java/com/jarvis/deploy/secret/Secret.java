package com.jarvis.deploy.secret;

import java.time.Instant;
import java.util.Objects;

/**
 * Represents a named secret value scoped to an environment or deployment.
 */
public class Secret {

    private final String key;
    private String value;
    private final SecretScope scope;
    private final String scopeTarget; // environment name or deployment id, null for GLOBAL
    private final Instant createdAt;
    private Instant updatedAt;

    public Secret(String key, String value, SecretScope scope, String scopeTarget) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Secret key must not be blank");
        }
        this.key = key;
        this.value = Objects.requireNonNull(value, "Secret value must not be null");
        this.scope = Objects.requireNonNull(scope, "Scope must not be null");
        this.scopeTarget = scopeTarget;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getKey() { return key; }

    public String getValue() { return value; }

    public SecretScope getScope() { return scope; }

    public String getScopeTarget() { return scopeTarget; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    public void updateValue(String newValue) {
        this.value = Objects.requireNonNull(newValue, "New value must not be null");
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "Secret{key='" + key + "', scope=" + scope +
               ", scopeTarget='" + scopeTarget + "'}";
    }
}
