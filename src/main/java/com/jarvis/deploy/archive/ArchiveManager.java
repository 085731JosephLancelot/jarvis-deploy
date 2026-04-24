package com.jarvis.deploy.archive;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages the lifecycle of deployment archives including creation,
 * retrieval, and pruning of old archive entries.
 */
public class ArchiveManager {

    private final Map<String, DeploymentArchive> archives = new ConcurrentHashMap<>();
    private final int maxArchivesPerEnvironment;

    public ArchiveManager(int maxArchivesPerEnvironment) {
        if (maxArchivesPerEnvironment <= 0) {
            throw new IllegalArgumentException("maxArchivesPerEnvironment must be positive");
        }
        this.maxArchivesPerEnvironment = maxArchivesPerEnvironment;
    }

    public DeploymentArchive archive(String deploymentId, String environment,
                                     String version, Instant deployedAt, ArchiveReason reason) {
        Objects.requireNonNull(deploymentId, "deploymentId must not be null");
        String archiveId = UUID.randomUUID().toString();
        DeploymentArchive archive = new DeploymentArchive(
                archiveId, deploymentId, environment, version, deployedAt, reason);
        archives.put(archiveId, archive);
        pruneIfNecessary(environment);
        return archive;
    }

    public Optional<DeploymentArchive> findById(String archiveId) {
        return Optional.ofNullable(archives.get(archiveId));
    }

    public List<DeploymentArchive> findByEnvironment(String environment) {
        return archives.values().stream()
                .filter(a -> a.getEnvironment().equals(environment))
                .sorted(Comparator.comparing(DeploymentArchive::getArchivedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<DeploymentArchive> findByReason(ArchiveReason reason) {
        return archives.values().stream()
                .filter(a -> a.getReason() == reason)
                .sorted(Comparator.comparing(DeploymentArchive::getArchivedAt).reversed())
                .collect(Collectors.toList());
    }

    public boolean remove(String archiveId) {
        return archives.remove(archiveId) != null;
    }

    public int count() {
        return archives.size();
    }

    private void pruneIfNecessary(String environment) {
        List<DeploymentArchive> envArchives = findByEnvironment(environment);
        if (envArchives.size() > maxArchivesPerEnvironment) {
            envArchives.stream()
                    .skip(maxArchivesPerEnvironment)
                    .forEach(a -> archives.remove(a.getArchiveId()));
        }
    }
}
