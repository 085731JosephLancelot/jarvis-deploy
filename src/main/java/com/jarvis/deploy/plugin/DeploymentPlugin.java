package com.jarvis.deploy.plugin;

import java.util.Map;

/**
 * Represents a deployable plugin that can hook into the deployment lifecycle.
 */
public class DeploymentPlugin {

    private final String id;
    private final String name;
    private final String version;
    private final PluginType type;
    private final Map<String, String> config;
    private boolean enabled;

    public DeploymentPlugin(String id, String name, String version, PluginType type, Map<String, String> config) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Plugin id must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Plugin name must not be blank");
        if (version == null || version.isBlank()) throw new IllegalArgumentException("Plugin version must not be blank");
        if (type == null) throw new IllegalArgumentException("Plugin type must not be null");
        this.id = id;
        this.name = name;
        this.version = version;
        this.type = type;
        this.config = config != null ? Map.copyOf(config) : Map.of();
        this.enabled = true;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public PluginType getType() { return type; }
    public Map<String, String> getConfig() { return config; }
    public boolean isEnabled() { return enabled; }

    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }

    public String getConfigValue(String key) {
        return config.get(key);
    }

    /**
     * Returns the config value for the given key, or the provided default if the key is absent.
     *
     * @param key          the config key to look up
     * @param defaultValue the value to return if the key is not present
     * @return the config value, or {@code defaultValue} if not found
     */
    public String getConfigValueOrDefault(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }

    @Override
    public String toString() {
        return String.format("DeploymentPlugin{id='%s', name='%s', version='%s', type=%s, enabled=%b}",
                id, name, version, type, enabled);
    }
}
