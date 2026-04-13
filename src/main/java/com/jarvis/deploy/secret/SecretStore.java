package com.jarvis.deploy.secret;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory store for managing secrets across scopes.
 * Keys are namespaced as "scope:scopeTarget:key" for uniqueness.
 */
public class SecretStore {

    private final Map<String, Secret> store = new ConcurrentHashMap<>();

    private String buildStoreKey(SecretScope scope, String scopeTarget, String key) {
        String target = (scopeTarget == null) ? "_" : scopeTarget;
        return scope.name() + ":" + target + ":" + key;
    }

    public void put(Secret secret) {
        Objects.requireNonNull(secret, "Secret must not be null");
        String storeKey = buildStoreKey(secret.getScope(), secret.getScopeTarget(), secret.getKey());
        store.put(storeKey, secret);
    }

    public Optional<Secret> get(SecretScope scope, String scopeTarget, String key) {
        String storeKey = buildStoreKey(scope, scopeTarget, key);
        return Optional.ofNullable(store.get(storeKey));
    }

    public boolean remove(SecretScope scope, String scopeTarget, String key) {
        String storeKey = buildStoreKey(scope, scopeTarget, key);
        return store.remove(storeKey) != null;
    }

    public List<Secret> listByScope(SecretScope scope, String scopeTarget) {
        String prefix = scope.name() + ":" + (scopeTarget == null ? "_" : scopeTarget) + ":";
        return store.entrySet().stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}
