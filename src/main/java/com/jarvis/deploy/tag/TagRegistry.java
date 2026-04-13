package com.jarvis.deploy.tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Central registry for managing deployment tags across environments.
 * Tags are keyed by environment and tag name for fast lookup.
 */
public class TagRegistry {

    // key: "environment::tagName" -> DeploymentTag
    private final Map<String, DeploymentTag> tags = new ConcurrentHashMap<>();

    public void register(DeploymentTag tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Tag must not be null");
        }
        String key = buildKey(tag.getEnvironment(), tag.getName());
        tags.put(key, tag);
    }

    public Optional<DeploymentTag> find(String environment, String name) {
        if (environment == null || name == null) return Optional.empty();
        return Optional.ofNullable(tags.get(buildKey(environment, name.trim().toLowerCase())));
    }

    public List<DeploymentTag> findByEnvironment(String environment) {
        if (environment == null) return Collections.emptyList();
        return tags.values().stream()
                .filter(t -> environment.equals(t.getEnvironment()))
                .collect(Collectors.toList());
    }

    public List<DeploymentTag> findByValue(String value) {
        if (value == null) return Collections.emptyList();
        return tags.values().stream()
                .filter(t -> value.equals(t.getValue()))
                .collect(Collectors.toList());
    }

    public boolean remove(String environment, String name) {
        String key = buildKey(environment, name == null ? "" : name.trim().toLowerCase());
        return tags.remove(key) != null;
    }

    public List<DeploymentTag> all() {
        return new ArrayList<>(tags.values());
    }

    public int size() {
        return tags.size();
    }

    public void clear() {
        tags.clear();
    }

    private String buildKey(String environment, String name) {
        return (environment == null ? "" : environment) + "::" + (name == null ? "" : name);
    }
}
