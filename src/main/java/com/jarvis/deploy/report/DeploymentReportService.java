package com.jarvis.deploy.report;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;
import com.jarvis.deploy.history.DeploymentHistoryEntry;
import com.jarvis.deploy.history.DeploymentHistoryService;
import com.jarvis.deploy.metrics.MetricsCollector;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating deployment summary reports.
 * Aggregates data from history, metrics, and deployment records
 * to produce human-readable and machine-consumable report snapshots.
 */
public class DeploymentReportService {

    private final DeploymentHistoryService historyService;
    private final MetricsCollector metricsCollector;

    public DeploymentReportService(DeploymentHistoryService historyService,
                                   MetricsCollector metricsCollector) {
        if (historyService == null) throw new IllegalArgumentException("historyService must not be null");
        if (metricsCollector == null) throw new IllegalArgumentException("metricsCollector must not be null");
        this.historyService = historyService;
        this.metricsCollector = metricsCollector;
    }

    /**
     * Generates a report covering all deployments within the last {@code days} days
     * for the specified environment.
     *
     * @param environment the target environment name
     * @param days        number of days to look back
     * @return a {@link DeploymentReport} summarising activity in the window
     */
    public DeploymentReport generateReport(String environment, int days) {
        if (environment == null || environment.isBlank()) {
            throw new IllegalArgumentException("environment must not be blank");
        }
        if (days <= 0) {
            throw new IllegalArgumentException("days must be a positive integer");
        }

        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
        Instant to = Instant.now();

        List<DeploymentHistoryEntry> entries = historyService
                .getHistory(environment)
                .stream()
                .filter(e -> !e.getTimestamp().isBefore(from))
                .collect(Collectors.toList());

        long total = entries.size();
        long successful = entries.stream()
                .filter(e -> e.getStatus() == DeploymentStatus.SUCCESS)
                .count();
        long failed = entries.stream()
                .filter(e -> e.getStatus() == DeploymentStatus.FAILED)
                .count();
        long rolledBack = entries.stream()
                .filter(e -> e.getStatus() == DeploymentStatus.ROLLED_BACK)
                .count();

        double successRate = total == 0 ? 0.0 : (successful * 100.0) / total;

        Map<String, Long> countByVersion = entries.stream()
                .collect(Collectors.groupingBy(
                        DeploymentHistoryEntry::getVersion,
                        Collectors.counting()));

        return DeploymentReport.builder()
                .environment(environment)
                .from(from)
                .to(to)
                .totalDeployments(total)
                .successfulDeployments(successful)
                .failedDeployments(failed)
                .rolledBackDeployments(rolledBack)
                .successRate(successRate)
                .deploymentsByVersion(countByVersion)
                .build();
    }

    /**
     * Formats a {@link DeploymentReport} as a plain-text summary suitable
     * for CLI output or log entries.
     *
     * @param report the report to format
     * @return formatted string representation
     */
    public String formatReport(DeploymentReport report) {
        return String.format(
                "=== Deployment Report: %s ===%n" +
                "Period : %s  ->  %s%n" +
                "Total  : %d%n" +
                "Success: %d (%.1f%%)%n" +
                "Failed : %d%n" +
                "Rolled : %d%n" +
                "By Version: %s%n",
                report.getEnvironment(),
                report.getFrom(),
                report.getTo(),
                report.getTotalDeployments(),
                report.getSuccessfulDeployments(),
                report.getSuccessRate(),
                report.getFailedDeployments(),
                report.getRolledBackDeployments(),
                report.getDeploymentsByVersion()
        );
    }
}
