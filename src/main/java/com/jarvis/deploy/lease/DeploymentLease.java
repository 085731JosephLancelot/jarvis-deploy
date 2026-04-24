package com.jarvis.deploy.lease;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a time-bounded lease on a deployment resource (e.g. an environment slot).
 * Leases prevent concurrent conflicting deployments without requiring a hard lock.
 */
public class DeploymentLease {

    private final String leaseId;
    private final String environment;
    private final String owner;
    private final Instant acquiredAt;
    private final Instant expiresAt;
    private LeaseStatus status;

    public DeploymentLease(String environment, String owner, long ttlSeconds) {
        Objects.requireNonNull(environment, "environment must not be null");
        Objects.requireNonNull(owner, "owner must not be null");
        if (ttlSeconds <= 0) throw new IllegalArgumentException("ttlSeconds must be positive");
        this.leaseId = UUID.randomUUID().toString();
        this.environment = environment;
        this.owner = owner;
        this.acquiredAt = Instant.now();
        this.expiresAt = acquiredAt.plusSeconds(ttlSeconds);
        this.status = LeaseStatus.ACTIVE;
    }

    public String getLeaseId() { return leaseId; }
    public String getEnvironment() { return environment; }
    public String getOwner() { return owner; }
    public Instant getAcquiredAt() { return acquiredAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public LeaseStatus getStatus() { return status; }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return status == LeaseStatus.ACTIVE && !isExpired();
    }

    public void release() {
        if (status == LeaseStatus.ACTIVE) {
            status = LeaseStatus.RELEASED;
        }
    }

    public void revoke() {
        status = LeaseStatus.REVOKED;
    }

    public void markExpired() {
        if (status == LeaseStatus.ACTIVE) {
            status = LeaseStatus.EXPIRED;
        }
    }

    @Override
    public String toString() {
        return "DeploymentLease{leaseId='" + leaseId + "', environment='" + environment +
                "', owner='" + owner + "', status=" + status +
                ", expiresAt=" + expiresAt + "}";
    }
}
