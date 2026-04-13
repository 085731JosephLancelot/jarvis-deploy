package com.jarvis.deploy.approval;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a deployment approval gate that must be cleared before
 * a deployment can proceed in a given environment.
 */
public class ApprovalGate {

    private final String id;
    private final String environment;
    private final String requestedBy;
    private final Instant requestedAt;
    private ApprovalStatus status;
    private String approvedBy;
    private Instant resolvedAt;
    private String comment;

    public ApprovalGate(String environment, String requestedBy) {
        this.id = UUID.randomUUID().toString();
        this.environment = Objects.requireNonNull(environment, "environment must not be null");
        this.requestedBy = Objects.requireNonNull(requestedBy, "requestedBy must not be null");
        this.requestedAt = Instant.now();
        this.status = ApprovalStatus.PENDING;
    }

    public void approve(String approver, String comment) {
        if (status != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Gate is not in PENDING state: " + status);
        }
        this.approvedBy = Objects.requireNonNull(approver, "approver must not be null");
        this.comment = comment;
        this.resolvedAt = Instant.now();
        this.status = ApprovalStatus.APPROVED;
    }

    public void reject(String approver, String comment) {
        if (status != ApprovalStatus.PENDING) {
            throw new IllegalStateException("Gate is not in PENDING state: " + status);
        }
        this.approvedBy = Objects.requireNonNull(approver, "approver must not be null");
        this.comment = comment;
        this.resolvedAt = Instant.now();
        this.status = ApprovalStatus.REJECTED;
    }

    public boolean isApproved() {
        return status == ApprovalStatus.APPROVED;
    }

    public String getId()          { return id; }
    public String getEnvironment() { return environment; }
    public String getRequestedBy() { return requestedBy; }
    public Instant getRequestedAt(){ return requestedAt; }
    public ApprovalStatus getStatus() { return status; }
    public String getApprovedBy()  { return approvedBy; }
    public Instant getResolvedAt() { return resolvedAt; }
    public String getComment()     { return comment; }

    @Override
    public String toString() {
        return String.format("ApprovalGate{id='%s', env='%s', status=%s, requestedBy='%s'}",
                id, environment, status, requestedBy);
    }
}
