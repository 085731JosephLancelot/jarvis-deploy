package com.jarvis.deploy.schedule;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Manages scheduled deployments for future execution.
 */
public class DeploymentScheduler {

    private static final Logger logger = Logger.getLogger(DeploymentScheduler.class.getName());

    private final ScheduledExecutorService executor;
    private final Map<String, ScheduledDeployment> scheduledJobs;
    private final Map<String, ScheduledFuture<?>> futures;

    public DeploymentScheduler() {
        this.executor = Executors.newScheduledThreadPool(4);
        this.scheduledJobs = new ConcurrentHashMap<>();
        this.futures = new ConcurrentHashMap<>();
    }

    public String schedule(ScheduledDeployment deployment) {
        if (deployment == null) {
            throw new IllegalArgumentException("Deployment cannot be null");
        }
        long delayMillis = deployment.getScheduledAt().toEpochMilli() - Instant.now().toEpochMilli();
        if (delayMillis < 0) {
            throw new IllegalArgumentException("Scheduled time must be in the future");
        }
        String id = deployment.getId();
        scheduledJobs.put(id, deployment);
        ScheduledFuture<?> future = executor.schedule(() -> {
            logger.info("Executing scheduled deployment: " + id);
            deployment.setStatus(ScheduleStatus.RUNNING);
            try {
                deployment.getTask().run();
                deployment.setStatus(ScheduleStatus.COMPLETED);
                logger.info("Scheduled deployment completed: " + id);
            } catch (Exception e) {
                deployment.setStatus(ScheduleStatus.FAILED);
                logger.severe("Scheduled deployment failed: " + id + " — " + e.getMessage());
            }
        }, delayMillis, TimeUnit.MILLISECONDS);
        futures.put(id, future);
        logger.info("Deployment scheduled: " + id + " at " + deployment.getScheduledAt());
        return id;
    }

    public boolean cancel(String deploymentId) {
        ScheduledFuture<?> future = futures.remove(deploymentId);
        if (future != null && !future.isDone()) {
            boolean cancelled = future.cancel(false);
            if (cancelled) {
                ScheduledDeployment job = scheduledJobs.get(deploymentId);
                if (job != null) job.setStatus(ScheduleStatus.CANCELLED);
                logger.info("Cancelled scheduled deployment: " + deploymentId);
            }
            return cancelled;
        }
        return false;
    }

    public Optional<ScheduledDeployment> find(String deploymentId) {
        return Optional.ofNullable(scheduledJobs.get(deploymentId));
    }

    public List<ScheduledDeployment> listPending() {
        List<ScheduledDeployment> pending = new ArrayList<>();
        for (ScheduledDeployment job : scheduledJobs.values()) {
            if (job.getStatus() == ScheduleStatus.PENDING) {
                pending.add(job);
            }
        }
        return Collections.unmodifiableList(pending);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
