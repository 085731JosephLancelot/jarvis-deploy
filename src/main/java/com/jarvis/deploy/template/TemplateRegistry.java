package com.jarvis.deploy.template;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for storing and retrieving deployment templates by ID.
 */
public class TemplateRegistry {

    private final Map<String, DeploymentTemplate> templates = new ConcurrentHashMap<>();

    /**
     * Registers a template. Overwrites any existing template with the same ID.
     */
    public void register(DeploymentTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template must not be null");
        }
        templates.put(template.getId(), template);
    }

    /**
     * Retrieves a template by its ID.
     */
    public Optional<DeploymentTemplate> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(templates.get(id));
    }

    /**
     * Returns all registered templates.
     */
    public List<DeploymentTemplate> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(templates.values()));
    }

    /**
     * Removes a template by ID. Returns true if it was present.
     */
    public boolean remove(String id) {
        if (id == null) return false;
        return templates.remove(id) != null;
    }

    /**
     * Returns the number of registered templates.
     */
    public int size() {
        return templates.size();
    }

    /**
     * Checks whether a template with the given ID exists.
     */
    public boolean contains(String id) {
        return id != null && templates.containsKey(id);
    }
}
