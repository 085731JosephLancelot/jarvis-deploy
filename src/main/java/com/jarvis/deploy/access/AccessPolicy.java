package com.jarvis.deploy.access;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class AccessPolicy {
    private final String principal;
    private final String environment;
    private final Set<AccessRole> roles;

    public AccessPolicy(String principal, String environment, Set<AccessRole> roles) {
        if (principal == null || principal.isBlank()) throw new IllegalArgumentException("Principal required");
        if (environment == null || environment.isBlank()) throw new IllegalArgumentException("Environment required");
        this.principal = principal;
        this.environment = environment;
        this.roles = roles.isEmpty() ? EnumSet.noneOf(AccessRole.class) : EnumSet.copyOf(roles);
    }

    public String getPrincipal() { return principal; }
    public String getEnvironment() { return environment; }
    public Set<AccessRole> getRoles() { return Collections.unmodifiableSet(roles); }

    public boolean hasRole(AccessRole role) { return roles.contains(role); }

    @Override
    public String toString() {
        return "AccessPolicy{principal='" + principal + "', environment='" + environment + "', roles=" + roles + "}";
    }
}
