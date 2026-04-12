package com.jarvis.deploy.config;

import com.jarvis.deploy.environment.Environment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages environment configurations for multi-environment deployments.
 * Provides registration, lookup, and validation of environments.
 */
public class EnvironmentConfig {

    private final Map<String, Environment> environments = new HashMap<>();

    /**
     * Registers an environment under its name.
     *
     * @param environment the environment to register
     * @throws IllegalArgumentException if environment is null or already registered
     */
    public void register(Environment environment) {
        if (environment == null) {
            throw new IllegalArgumentException("Environment must not be null");
        }
        String name = environment.getName();
        if (environments.containsKey(name)) {
            throw new IllegalArgumentException("Environment already registered: " + name);
        }
        environments.put(name, environment);
    }

    /**
     * Retrieves an environment by name.
     *
     * @param name the environment name
     * @return an Optional containing the environment, or empty if not found
     */
    public Optional<Environment> get(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(environments.get(name));
    }

    /**
     * Returns all registered environment names.
     *
     * @return unmodifiable set of environment names
     */
    public Set<String> listEnvironments() {
        return Collections.unmodifiableSet(environments.keySet());
    }

    /**
     * Checks whether an environment with the given name is registered.
     *
     * @param name the environment name
     * @return true if registered, false otherwise
     */
    public boolean contains(String name) {
        return name != null && environments.containsKey(name);
    }

    /**
     * Removes an environment by name.
     *
     * @param name the environment name
     * @return true if removed, false if not found
     */
    public boolean deregister(String name) {
        return environments.remove(name) != null;
    }

    /**
     * Returns the total number of registered environments.
     */
    public int size() {
        return environments.size();
    }
}
