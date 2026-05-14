package com.jarvis.deploy.token;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a short-lived token issued for a specific deployment operation.
 */
public class DeploymentToken {

    private final String tokenId;
    private final String environment;
    private final String issuedBy;
    private final Instant issuedAt;
    private final Instant expiresAt;
    private boolean revoked;

    public DeploymentToken(String environment, String issuedBy, long ttlSeconds) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(issuedBy, "issuedBy must not be null");
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be positive");
        }
        this.tokenId = UUID.randomUUID().toString();
        this.environment = environment;
        this.issuedBy = issuedBy;
        this.issuedAt = Instant.now();
        this.expiresAt = issuedAt.plusSeconds(ttlSeconds);
        this.revoked = false;
    }

    public String getTokenId() { return tokenId; }
    public String getEnvironment() { return environment; }
    public String getIssuedBy() { return issuedBy; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRevoked() { return revoked; }

    public void revoke() {
        this.revoked = true;
    }

    public boolean isValid() {
        return !revoked && Instant.now().isBefore(expiresAt);
    }

    @Override
    public String toString() {
        return "DeploymentToken{tokenId='" + tokenId + "', environment='" + environment +
               "', issuedBy='" + issuedBy + "', expiresAt=" + expiresAt +
               ", revoked=" + revoked + "}";
    }
}
