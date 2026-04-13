package com.jarvis.deploy.validation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result of a deployment validation containing any constraint violations.
 */
public class ValidationResult {

    private final List<String> violations;

    public ValidationResult(List<String> violations) {
        Objects.requireNonNull(violations, "Violations list must not be null");
        this.violations = Collections.unmodifiableList(violations);
    }

    public boolean isValid() {
        return violations.isEmpty();
    }

    public List<String> getViolations() {
        return violations;
    }

    public String getSummary() {
        if (isValid()) {
            return "Validation passed with no violations.";
        }
        StringBuilder sb = new StringBuilder("Validation failed with ");
        sb.append(violations.size()).append(" violation(s):\n");
        violations.forEach(v -> sb.append("  - ").append(v).append("\n"));
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return getSummary();
    }
}
