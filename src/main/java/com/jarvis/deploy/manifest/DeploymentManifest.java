package com.jarvis.deploy.manifest;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a deployment manifest containing metadata and configuration
 * for a specific deployment artifact version.
 */
public class DeploymentManifest {

    private final String manifestId;
    private final String applicationName;
    private final String version;
    private final String environment;
    private final Map<String, String> properties;
    private final Instant createdAt;
    private String checksum;

    public DeploymentManifest(String manifestId, String applicationName,
                               String version, String environment) {
        Objects.requireNonNull(manifestId, "manifestId must not be null");
        Objects.requireNonNull(applicationName, "applicationName must not be null");
        Objects.requireNonNull(version, "version must not be null");
        Objects.requireNonNull(environment, "environment must not be null");
        this.manifestId = manifestId;
        this.applicationName = applicationName;
        this.version = version;
        this.environment = environment;
        this.properties = new HashMap<>();
        this.createdAt = Instant.now();
    }

    public void addProperty(String key, String value) {
        Objects.requireNonNull(key, "key must not be null");
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getManifestId() { return manifestId; }
    public String getApplicationName() { return applicationName; }
    public String getVersion() { return version; }
    public String getEnvironment() { return environment; }
    public Map<String, String> getProperties() { return Collections.unmodifiableMap(properties); }
    public Instant getCreatedAt() { return createdAt; }
    public String getChecksum() { return checksum; }

    @Override
    public String toString() {
        return String.format("DeploymentManifest{id='%s', app='%s', version='%s', env='%s'}",
                manifestId, applicationName, version, environment);
    }
}
