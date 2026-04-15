package com.jarvis.deploy.history;

import com.jarvis.deploy.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service for recording and querying deployment history.
 */
public class DeploymentHistoryService {

    private final List<DeploymentHistoryEntry> entries = new CopyOnWriteArrayList<>();

    public DeploymentHistoryEntry record(String deploymentId, String environment,
                                          String version, DeploymentStatus status,
                                          String triggeredBy, String notes) {
        String entryId = UUID.randomUUID().toString();
        DeploymentHistoryEntry entry = new DeploymentHistoryEntry(
                entryId, deploymentId, environment, version,
                status, triggeredBy, Instant.now(), notes);
        entries.add(entry);
        return entry;
    }

    public List<DeploymentHistoryEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public List<DeploymentHistoryEntry> getByEnvironment(String environment) {
        Objects.requireNonNull(environment, "environment must not be null");
        return entries.stream()
                .filter(e -> e.getEnvironment().equalsIgnoreCase(environment))
                .collect(Collectors.toList());
    }

    public List<DeploymentHistoryEntry> getByDeploymentId(String deploymentId) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        return entries.stream()
                .filter(e -> e.getDeploymentId().equals(deploymentId))
                .collect(Collectors.toList());
    }

    public List<DeploymentHistoryEntry> getByStatus(DeploymentStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return entries.stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toList());
    }

    public Optional<DeploymentHistoryEntry> getLatestForEnvironment(String environment) {
        return getByEnvironment(environment).stream()
                .max(Comparator.comparing(DeploymentHistoryEntry::getTimestamp));
    }

    public int getTotalCount() {
        return entries.size();
    }

    public void clear() {
        entries.clear();
    }
}
