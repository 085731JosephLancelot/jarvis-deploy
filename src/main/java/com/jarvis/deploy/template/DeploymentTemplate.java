package com.jarvis.deploy.template;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a reusable deployment template with parameterized configuration.
 */
public class DeploymentTemplate {

    private final String id;
    private final String name;
    private final String description;
    private final Map<String, String> defaultParameters;
    private final String version;

    public DeploymentTemplate(String id, String name, String description,
                               Map<String, String> defaultParameters, String version) {
        Objects.requireNonNull(id, "Template id must not be null");
        Objects.requireNonNull(name, "Template name must not be null");
        Objects.requireNonNull(version, "Template version must not be null");
        this.id = id;
        this.name = name;
        this.description = description != null ? description : "";
        this.defaultParameters = defaultParameters != null
                ? Collections.unmodifiableMap(new HashMap<>(defaultParameters))
                : Collections.emptyMap();
        this.version = version;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getVersion() { return version; }
    public Map<String, String> getDefaultParameters() { return defaultParameters; }

    /**
     * Merges override parameters on top of the template defaults.
     */
    public Map<String, String> resolveParameters(Map<String, String> overrides) {
        Map<String, String> resolved = new HashMap<>(defaultParameters);
        if (overrides != null) {
            resolved.putAll(overrides);
        }
        return Collections.unmodifiableMap(resolved);
    }

    @Override
    public String toString() {
        return "DeploymentTemplate{id='" + id + "', name='" + name +
                "', version='" + version + "'}";
    }
}
