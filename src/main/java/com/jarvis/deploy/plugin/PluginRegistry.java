package com.jarvis.deploy.plugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Registry for managing registered deployment plugins.
 */
public class PluginRegistry {

    private final Map<String, DeploymentPlugin> plugins = new LinkedHashMap<>();

    public void register(DeploymentPlugin plugin) {
        if (plugin == null) throw new IllegalArgumentException("Plugin must not be null");
        if (plugins.containsKey(plugin.getId())) {
            throw new IllegalStateException("Plugin already registered with id: " + plugin.getId());
        }
        plugins.put(plugin.getId(), plugin);
    }

    public void unregister(String pluginId) {
        if (!plugins.containsKey(pluginId)) {
            throw new NoSuchElementException("No plugin found with id: " + pluginId);
        }
        plugins.remove(pluginId);
    }

    public Optional<DeploymentPlugin> findById(String pluginId) {
        return Optional.ofNullable(plugins.get(pluginId));
    }

    public List<DeploymentPlugin> findByType(PluginType type) {
        return plugins.values().stream()
                .filter(p -> p.getType() == type)
                .collect(Collectors.toList());
    }

    public List<DeploymentPlugin> findEnabled() {
        return plugins.values().stream()
                .filter(DeploymentPlugin::isEnabled)
                .collect(Collectors.toList());
    }

    public List<DeploymentPlugin> findEnabledByType(PluginType type) {
        return plugins.values().stream()
                .filter(p -> p.isEnabled() && p.getType() == type)
                .collect(Collectors.toList());
    }

    public int size() {
        return plugins.size();
    }

    public boolean contains(String pluginId) {
        return plugins.containsKey(pluginId);
    }

    public Collection<DeploymentPlugin> all() {
        return Collections.unmodifiableCollection(plugins.values());
    }
}
