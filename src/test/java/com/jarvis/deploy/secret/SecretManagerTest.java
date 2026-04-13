package com.jarvis.deploy.secret;

import com.jarvis.deploy.audit.AuditLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretManagerTest {

    @Mock
    private AuditLogger auditLogger;

    private SecretStore store;
    private SecretManager manager;

    @BeforeEach
    void setUp() {
        store = new SecretStore();
        manager = new SecretManager(store, auditLogger);
    }

    @Test
    void setSecret_createsNewSecret() {
        manager.setSecret("DB_PASS", "s3cr3t", SecretScope.ENVIRONMENT, "production");

        Optional<String> value = manager.getSecretValue("DB_PASS", SecretScope.ENVIRONMENT, "production");
        assertThat(value).isPresent().contains("s3cr3t");
        verify(auditLogger).log(any());
    }

    @Test
    void setSecret_updatesExistingSecret() {
        manager.setSecret("API_KEY", "old", SecretScope.GLOBAL, null);
        manager.setSecret("API_KEY", "new", SecretScope.GLOBAL, null);

        Optional<String> value = manager.getSecretValue("API_KEY", SecretScope.GLOBAL, null);
        assertThat(value).isPresent().contains("new");
        verify(auditLogger, times(2)).log(any());
    }

    @Test
    void getSecretValue_returnsEmptyWhenNotFound() {
        Optional<String> value = manager.getSecretValue("MISSING", SecretScope.DEPLOYMENT, "dep-1");
        assertThat(value).isEmpty();
    }

    @Test
    void deleteSecret_removesAndAudits() {
        manager.setSecret("TOKEN", "abc", SecretScope.ENVIRONMENT, "staging");
        boolean removed = manager.deleteSecret("TOKEN", SecretScope.ENVIRONMENT, "staging");

        assertThat(removed).isTrue();
        assertThat(manager.getSecretValue("TOKEN", SecretScope.ENVIRONMENT, "staging")).isEmpty();
        verify(auditLogger, times(2)).log(any());
    }

    @Test
    void deleteSecret_returnsFalseWhenNotFound() {
        boolean removed = manager.deleteSecret("GHOST", SecretScope.GLOBAL, null);
        assertThat(removed).isFalse();
        verify(auditLogger, never()).log(any());
    }

    @Test
    void listSecrets_returnsOnlyMatchingScope() {
        manager.setSecret("K1", "v1", SecretScope.ENVIRONMENT, "prod");
        manager.setSecret("K2", "v2", SecretScope.ENVIRONMENT, "prod");
        manager.setSecret("K3", "v3", SecretScope.ENVIRONMENT, "staging");

        List<Secret> prodSecrets = manager.listSecrets(SecretScope.ENVIRONMENT, "prod");
        assertThat(prodSecrets).hasSize(2)
                .extracting(Secret::getKey)
                .containsExactlyInAnyOrder("K1", "K2");
    }
}
