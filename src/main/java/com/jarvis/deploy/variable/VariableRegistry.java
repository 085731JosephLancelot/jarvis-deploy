package com.jarvis.deploy.variable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registry for managing deployment variables per environment.
 */
public class VariableRegistry {

    private final Map<String, DeploymentVariable> variables = new ConcurrentHashMap<>();

    private String compositeKey(String name, String environment) {
        return environment + ":" + name;
    }

    public void register(DeploymentVariable variable) {
        Objects.requireNonNull(variable, "Variable must not be null");
        variables.put(compositeKey(variable.getName(), variable.getEnvironment()), variable);
    }

    public Optional<DeploymentVariable> get(String name, String environment) {
        return Optional.ofNullable(variables.get(compositeKey(name, environment)));
    }

    public boolean update(String name, String environment, String newValue) {
        String key = compositeKey(name, environment);
        DeploymentVariable variable = variables.get(key);
        if (variable == null) return false;
        variable.setValue(newValue);
        return true;
    }

    public boolean remove(String name, String environment) {
        return variables.remove(compositeKey(name, environment)) != null;
    }

    public List<DeploymentVariable> listByEnvironment(String environment) {
        return variables.values().stream()
                .filter(v -> v.getEnvironment().equals(environment))
                .collect(Collectors.toList());
    }

    public List<DeploymentVariable> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(variables.values()));
    }

    public int size() {
        return variables.size();
    }

    public void clear() {
        variables.clear();
    }
}
