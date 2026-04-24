package com.jarvis.deploy.lease;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages deployment leases per environment. Only one active lease per environment
 * is permitted at a time. Expired leases are evicted automatically on access.
 */
public class LeaseManager {

    private static final long DEFAULT_TTL_SECONDS = 300L;

    private final Map<String, DeploymentLease> leases = new ConcurrentHashMap<>();
    private final long defaultTtlSeconds;

    public LeaseManager() {
        this(DEFAULT_TTL_SECONDS);
    }

    public LeaseManager(long defaultTtlSeconds) {
        if (defaultTtlSeconds <= 0) throw new IllegalArgumentException("defaultTtlSeconds must be positive");
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    /**
     * Attempts to acquire a lease for the given environment.
     *
     * @return the new lease if successful
     * @throws LeaseConflictException if an active lease already exists
     */
    public DeploymentLease acquire(String environment, String owner) {
        return acquire(environment, owner, defaultTtlSeconds);
    }

    public DeploymentLease acquire(String environment, String owner, long ttlSeconds) {
        evictIfExpired(environment);
        DeploymentLease existing = leases.get(environment);
        if (existing != null && existing.isActive()) {
            throw new LeaseConflictException(
                "Environment '" + environment + "' is already leased by '" + existing.getOwner() +
                "' until " + existing.getExpiresAt());
        }
        DeploymentLease lease = new DeploymentLease(environment, owner, ttlSeconds);
        leases.put(environment, lease);
        return lease;
    }

    public boolean release(String environment, String owner) {
        DeploymentLease lease = leases.get(environment);
        if (lease == null || !lease.getOwner().equals(owner)) return false;
        lease.release();
        leases.remove(environment);
        return true;
    }

    public boolean revoke(String environment) {
        DeploymentLease lease = leases.remove(environment);
        if (lease == null) return false;
        lease.revoke();
        return true;
    }

    public Optional<DeploymentLease> current(String environment) {
        evictIfExpired(environment);
        return Optional.ofNullable(leases.get(environment)).filter(DeploymentLease::isActive);
    }

    public boolean isLeased(String environment) {
        return current(environment).isPresent();
    }

    public int activeLeaseCount() {
        leases.entrySet().removeIf(e -> !e.getValue().isActive());
        return leases.size();
    }

    private void evictIfExpired(String environment) {
        DeploymentLease lease = leases.get(environment);
        if (lease != null && lease.isExpired()) {
            lease.markExpired();
            leases.remove(environment);
        }
    }
}
