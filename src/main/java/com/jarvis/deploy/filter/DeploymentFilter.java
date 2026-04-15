package com.jarvis.deploy.filter;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Provides chainable filtering criteria for querying deployments.
 */
public class DeploymentFilter {

    private String environment;
    private String serviceName;
    private DeploymentStatus status;
    private Instant fromTime;
    private Instant toTime;
    private String deployedBy;

    private DeploymentFilter() {}

    public static DeploymentFilter newFilter() {
        return new DeploymentFilter();
    }

    public DeploymentFilter withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    public DeploymentFilter withServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public DeploymentFilter withStatus(DeploymentStatus status) {
        this.status = status;
        return this;
    }

    public DeploymentFilter fromTime(Instant fromTime) {
        this.fromTime = fromTime;
        return this;
    }

    public DeploymentFilter toTime(Instant toTime) {
        this.toTime = toTime;
        return this;
    }

    public DeploymentFilter deployedBy(String deployedBy) {
        this.deployedBy = deployedBy;
        return this;
    }

    public List<Predicate<Deployment>> buildPredicates() {
        List<Predicate<Deployment>> predicates = new ArrayList<>();
        if (environment != null) {
            predicates.add(d -> environment.equals(d.getEnvironment()));
        }
        if (serviceName != null) {
            predicates.add(d -> serviceName.equals(d.getServiceName()));
        }
        if (status != null) {
            predicates.add(d -> status.equals(d.getStatus()));
        }
        if (fromTime != null) {
            predicates.add(d -> !d.getCreatedAt().isBefore(fromTime));
        }
        if (toTime != null) {
            predicates.add(d -> !d.getCreatedAt().isAfter(toTime));
        }
        if (deployedBy != null) {
            predicates.add(d -> deployedBy.equals(d.getDeployedBy()));
        }
        return predicates;
    }

    public List<Deployment> apply(List<Deployment> deployments) {
        Objects.requireNonNull(deployments, "Deployments list must not be null");
        List<Predicate<Deployment>> predicates = buildPredicates();
        return deployments.stream()
                .filter(d -> predicates.stream().allMatch(p -> p.test(d)))
                .collect(Collectors.toList());
    }
}
