package com.jarvis.deploy.filter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Registry for storing and retrieving named DeploymentFilter presets.
 */
public class FilterRegistry {

    private final Map<String, DeploymentFilter> filters = new LinkedHashMap<>();

    /**
     * Registers a named filter preset.
     *
     * @param name   unique name for the filter
     * @param filter the filter to register
     * @throws IllegalArgumentException if name or filter is null, or name already exists
     */
    public void register(String name, DeploymentFilter filter) {
        Objects.requireNonNull(name, "Filter name must not be null");
        Objects.requireNonNull(filter, "Filter must not be null");
        if (filters.containsKey(name)) {
            throw new IllegalArgumentException("Filter already registered with name: " + name);
        }
        filters.put(name, filter);
    }

    /**
     * Retrieves a registered filter by name.
     *
     * @param name the filter name
     * @return an Optional containing the filter, or empty if not found
     */
    public Optional<DeploymentFilter> get(String name) {
        Objects.requireNonNull(name, "Filter name must not be null");
        return Optional.ofNullable(filters.get(name));
    }

    /**
     * Removes a registered filter by name.
     *
     * @param name the filter name
     * @return true if removed, false if not found
     */
    public boolean remove(String name) {
        Objects.requireNonNull(name, "Filter name must not be null");
        return filters.remove(name) != null;
    }

    /**
     * Returns all registered filter names.
     */
    public Map<String, DeploymentFilter> all() {
        return Collections.unmodifiableMap(filters);
    }

    /**
     * Returns the number of registered filters.
     */
    public int size() {
        return filters.size();
    }

    /**
     * Clears all registered filters.
     */
    public void clear() {
        filters.clear();
    }
}
