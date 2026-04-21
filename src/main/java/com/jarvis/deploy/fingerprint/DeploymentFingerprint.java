package com.jarvis.deploy.fingerprint;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Represents a deterministic fingerprint of a deployment's configuration and artifacts.
 * Used to detect duplicate or unchanged deployments before execution.
 */
public class DeploymentFingerprint {

    private final String deploymentId;
    private final String environment;
    private final String artifactVersion;
    private final Map<String, String> configEntries;
    private final String hash;
    private final Instant computedAt;

    public DeploymentFingerprint(String deploymentId, String environment,
                                  String artifactVersion, Map<String, String> configEntries) {
        this.deploymentId = Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.artifactVersion = Objects.requireNonNull(artifactVersion, "artifactVersion must not be null");
        this.configEntries = Collections.unmodifiableMap(new TreeMap<>(configEntries));
        this.computedAt = Instant.now();
        this.hash = computeHash();
    }

    private String computeHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            StringBuilder raw = new StringBuilder();
            raw.append(deploymentId).append("|");
            raw.append(environment).append("|");
            raw.append(artifactVersion).append("|");
            configEntries.forEach((k, v) -> raw.append(k).append("=").append(v).append(";"));
            byte[] hashBytes = digest.digest(raw.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public boolean matches(DeploymentFingerprint other) {
        if (other == null) return false;
        return this.hash.equals(other.hash);
    }

    public String getDeploymentId() { return deploymentId; }
    public String getEnvironment() { return environment; }
    public String getArtifactVersion() { return artifactVersion; }
    public Map<String, String> getConfigEntries() { return configEntries; }
    public String getHash() { return hash; }
    public Instant getComputedAt() { return computedAt; }

    @Override
    public String toString() {
        return "DeploymentFingerprint{deploymentId='" + deploymentId + "', environment='" + environment +
                "', artifactVersion='" + artifactVersion + "', hash='" + hash + "'}";
    }
}
