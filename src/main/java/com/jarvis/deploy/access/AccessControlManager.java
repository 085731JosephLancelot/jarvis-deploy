package com.jarvis.deploy.access;

import java.util.*;

public class AccessControlManager {
    private final Map<String, AccessPolicy> policies = new LinkedHashMap<>();

    private String key(String principal, String environment) {
        return principal + "@" + environment;
    }

    public void grant(AccessPolicy policy) {
        policies.put(key(policy.getPrincipal(), policy.getEnvironment()), policy);
    }

    public void revoke(String principal, String environment) {
        policies.remove(key(principal, environment));
    }

    public boolean isAllowed(String principal, String environment, AccessRole role) {
        AccessPolicy policy = policies.get(key(principal, environment));
        return policy != null && policy.hasRole(role);
    }

    public Optional<AccessPolicy> getPolicy(String principal, String environment) {
        return Optional.ofNullable(policies.get(key(principal, environment)));
    }

    public List<AccessPolicy> getPoliciesForEnvironment(String environment) {
        List<AccessPolicy> result = new ArrayList<>();
        for (AccessPolicy p : policies.values()) {
            if (p.getEnvironment().equals(environment)) result.add(p);
        }
        return Collections.unmodifiableList(result);
    }

    public List<AccessPolicy> getPoliciesForPrincipal(String principal) {
        List<AccessPolicy> result = new ArrayList<>();
        for (AccessPolicy p : policies.values()) {
            if (p.getPrincipal().equals(principal)) result.add(p);
        }
        return Collections.unmodifiableList(result);
    }

    public int size() { return policies.size(); }
}
