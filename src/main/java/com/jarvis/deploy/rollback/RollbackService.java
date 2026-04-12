package com.jarvis.deploy.rollback;

import com.jarvis.deploy.audit.AuditEvent;
import com.jarvis.deploy.audit.AuditLogger;
import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentService;
import com.jarvis.deploy.deployment.DeploymentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for rolling back deployments to a previous stable version.
 */
public class RollbackService {

    private final DeploymentService deploymentService;
    private final AuditLogger auditLogger;

    public RollbackService(DeploymentService deploymentService, AuditLogger auditLogger) {
        this.deploymentService = deploymentService;
        this.auditLogger = auditLogger;
    }

    /**
     * Rolls back the given environment to the most recent successful deployment
     * that precedes the currently active deployment.
     *
     * @param environment the target environment name
     * @return the deployment that was restored
     * @throws RollbackException if no suitable rollback target is found
     */
    public Deployment rollback(String environment) {
        List<Deployment> history = deploymentService.getDeploymentHistory(environment);

        Optional<Deployment> current = history.stream()
                .filter(d -> d.getStatus() == DeploymentStatus.SUCCESS)
                .findFirst();

        if (current.isEmpty()) {
            throw new RollbackException("No successful deployment found for environment: " + environment);
        }

        int currentIndex = history.indexOf(current.get());

        Optional<Deployment> target = history.stream()
                .skip(currentIndex + 1)
                .filter(d -> d.getStatus() == DeploymentStatus.SUCCESS)
                .findFirst();

        if (target.isEmpty()) {
            throw new RollbackException(
                    "No previous successful deployment available to roll back to in environment: " + environment);
        }

        Deployment rollbackTarget = target.get();
        deploymentService.promoteDeployment(environment, rollbackTarget.getVersion());

        auditLogger.log(new AuditEvent(
                "ROLLBACK",
                environment,
                "Rolled back to version " + rollbackTarget.getVersion()
                        + " from " + current.get().getVersion()));

        return rollbackTarget;
    }
}
