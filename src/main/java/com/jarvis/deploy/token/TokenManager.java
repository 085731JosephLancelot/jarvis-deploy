package com.jarvis.deploy.token;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages issuance, lookup, validation, and revocation of deployment tokens.
 */
public class TokenManager {

    private final Map<String, DeploymentToken> tokenStore = new ConcurrentHashMap<>();
    private final long defaultTtlSeconds;

    public TokenManager(long defaultTtlSeconds) {
        if (defaultTtlSeconds <= 0) {
            throw new IllegalArgumentException("defaultTtlSeconds must be positive");
        }
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public DeploymentToken issue(String environment, String issuedBy) {
        DeploymentToken token = new DeploymentToken(environment, issuedBy, defaultTtlSeconds);
        tokenStore.put(token.getTokenId(), token);
        return token;
    }

    public DeploymentToken issue(String environment, String issuedBy, long ttlSeconds) {
        DeploymentToken token = new DeploymentToken(environment, issuedBy, ttlSeconds);
        tokenStore.put(token.getTokenId(), token);
        return token;
    }

    public Optional<DeploymentToken> find(String tokenId) {
        return Optional.ofNullable(tokenStore.get(tokenId));
    }

    public boolean validate(String tokenId) {
        return find(tokenId).map(DeploymentToken::isValid).orElse(false);
    }

    public boolean revoke(String tokenId) {
        DeploymentToken token = tokenStore.get(tokenId);
        if (token == null) return false;
        token.revoke();
        return true;
    }

    public Collection<DeploymentToken> listActive() {
        return tokenStore.values().stream()
                .filter(DeploymentToken::isValid)
                .collect(Collectors.toList());
    }

    public int purgeExpired() {
        int before = tokenStore.size();
        tokenStore.entrySet().removeIf(e -> !e.getValue().isValid());
        return before - tokenStore.size();
    }

    public int size() {
        return tokenStore.size();
    }
}
