package com.jarvis.deploy.secret;

import com.jarvis.deploy.audit.AuditEvent;
import com.jarvis.deploy.audit.AuditLogger;

import java.util.List;
import java.util.Optional;

/**
 * High-level API for secret management with audit logging.
 */
public class SecretManager {

    private final SecretStore store;
    private final AuditLogger auditLogger;

    public SecretManager(SecretStore store, AuditLogger auditLogger) {
        this.store = store;
        this.auditLogger = auditLogger;
    }

    public void setSecret(String key, String value, SecretScope scope, String scopeTarget) {
        Optional<Secret> existing = store.get(scope, scopeTarget, key);
        if (existing.isPresent()) {
            existing.get().updateValue(value);
            auditLogger.log(new AuditEvent("SECRET_UPDATED",
                    "Updated secret '" + key + "' in scope " + scope + ":" + scopeTarget));
        } else {
            Secret secret = new Secret(key, value, scope, scopeTarget);
            store.put(secret);
            auditLogger.log(new AuditEvent("SECRET_CREATED",
                    "Created secret '" + key + "' in scope " + scope + ":" + scopeTarget));
        }
    }

    public Optional<String> getSecretValue(String key, SecretScope scope, String scopeTarget) {
        return store.get(scope, scopeTarget, key).map(Secret::getValue);
    }

    public boolean deleteSecret(String key, SecretScope scope, String scopeTarget) {
        boolean removed = store.remove(scope, scopeTarget, key);
        if (removed) {
            auditLogger.log(new AuditEvent("SECRET_DELETED",
                    "Deleted secret '" + key + "' from scope " + scope + ":" + scopeTarget));
        }
        return removed;
    }

    public List<Secret> listSecrets(SecretScope scope, String scopeTarget) {
        return store.listByScope(scope, scopeTarget);
    }
}
