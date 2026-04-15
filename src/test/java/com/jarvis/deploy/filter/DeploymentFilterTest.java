package com.jarvis.deploy.filter;

import com.jarvis.deploy.deployment.Deployment;
import com.jarvis.deploy.deployment.DeploymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentFilterTest {

    private Deployment dep1;
    private Deployment dep2;
    private Deployment dep3;
    private List<Deployment> all;
    private FilterRegistry registry;

    @BeforeEach
    void setUp() {
        Instant base = Instant.parse("2024-01-10T10:00:00Z");
        dep1 = new Deployment("svc-a", "1.0.0", "production", "alice");
        dep1.setStatus(DeploymentStatus.SUCCESS);
        dep1.setCreatedAt(base);

        dep2 = new Deployment("svc-b", "2.0.0", "staging", "bob");
        dep2.setStatus(DeploymentStatus.FAILED);
        dep2.setCreatedAt(base.plusSeconds(3600));

        dep3 = new Deployment("svc-a", "1.1.0", "production", "alice");
        dep3.setStatus(DeploymentStatus.FAILED);
        dep3.setCreatedAt(base.plusSeconds(7200));

        all = Arrays.asList(dep1, dep2, dep3);
        registry = new FilterRegistry();
    }

    @Test
    void filterByEnvironment() {
        List<Deployment> result = DeploymentFilter.newFilter()
                .withEnvironment("production")
                .apply(all);
        assertEquals(2, result.size());
        assertTrue(result.contains(dep1));
        assertTrue(result.contains(dep3));
    }

    @Test
    void filterByStatusAndEnvironment() {
        List<Deployment> result = DeploymentFilter.newFilter()
                .withEnvironment("production")
                .withStatus(DeploymentStatus.FAILED)
                .apply(all);
        assertEquals(1, result.size());
        assertTrue(result.contains(dep3));
    }

    @Test
    void filterByDeployedBy() {
        List<Deployment> result = DeploymentFilter.newFilter()
                .deployedBy("alice")
                .apply(all);
        assertEquals(2, result.size());
    }

    @Test
    void filterByTimeRange() {
        Instant from = Instant.parse("2024-01-10T10:30:00Z");
        Instant to   = Instant.parse("2024-01-10T12:30:00Z");
        List<Deployment> result = DeploymentFilter.newFilter()
                .fromTime(from)
                .toTime(to)
                .apply(all);
        assertEquals(2, result.size());
        assertTrue(result.contains(dep2));
        assertTrue(result.contains(dep3));
    }

    @Test
    void emptyFilterReturnsAll() {
        List<Deployment> result = DeploymentFilter.newFilter().apply(all);
        assertEquals(3, result.size());
    }

    @Test
    void registryStoresAndRetrievesFilter() {
        DeploymentFilter f = DeploymentFilter.newFilter().withEnvironment("production");
        registry.register("prod-only", f);
        Optional<DeploymentFilter> found = registry.get("prod-only");
        assertTrue(found.isPresent());
        assertEquals(2, found.get().apply(all).size());
    }

    @Test
    void registryThrowsOnDuplicateName() {
        DeploymentFilter f = DeploymentFilter.newFilter();
        registry.register("my-filter", f);
        assertThrows(IllegalArgumentException.class, () -> registry.register("my-filter", f));
    }

    @Test
    void registryRemovesFilter() {
        registry.register("temp", DeploymentFilter.newFilter());
        assertTrue(registry.remove("temp"));
        assertFalse(registry.get("temp").isPresent());
    }

    @Test
    void applyThrowsOnNullList() {
        assertThrows(NullPointerException.class,
                () -> DeploymentFilter.newFilter().apply(null));
    }
}
