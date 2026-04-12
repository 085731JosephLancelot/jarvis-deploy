package com.jarvis.deploy.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLoggerTest {

    @TempDir
    Path tempDir;

    @Test
    void recordAddsEventToSessionList() {
        AuditLogger logger = new AuditLogger(tempDir.resolve("audit/deploy.log"));

        AuditEvent event = logger.record(
                AuditEvent.EventType.DEPLOY, "staging",
                "payment-service", "1.4.2",
                "ci-bot", "Automated deploy from pipeline");

        List<AuditEvent> events = logger.getSessionEvents();
        assertEquals(1, events.size());
        assertSame(event, events.get(0));
        assertEquals(AuditEvent.EventType.DEPLOY, event.getType());
        assertEquals("staging", event.getEnvironment());
        assertEquals("payment-service", event.getService());
        assertEquals("1.4.2", event.getVersion());
        assertEquals("ci-bot", event.getInitiatedBy());
        assertNotNull(event.getId());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void recordPersistsEventToFile() throws IOException {
        Path logFile = tempDir.resolve("audit/deploy.log");
        AuditLogger logger = new AuditLogger(logFile);

        logger.record(AuditEvent.EventType.ROLLBACK, "production",
                "auth-service", "2.0.1", "admin", "Emergency rollback");

        assertTrue(Files.exists(logFile));
        List<String> lines = Files.readAllLines(logFile);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("ROLLBACK"));
        assertTrue(lines.get(0).contains("production"));
        assertTrue(lines.get(0).contains("auth-service"));
    }

    @Test
    void multipleEventsAppendToFile() throws IOException {
        Path logFile = tempDir.resolve("logs/audit.log");
        AuditLogger logger = new AuditLogger(logFile);

        logger.record(AuditEvent.EventType.DEPLOY, "dev", "svc", "1.0", "user", null);
        logger.record(AuditEvent.EventType.PROMOTE, "staging", "svc", "1.0", "user", null);

        assertEquals(2, logger.getSessionEvents().size());
        assertEquals(2, Files.readAllLines(logFile).size());
    }

    @Test
    void sessionEventsListIsImmutable() {
        AuditLogger logger = new AuditLogger(tempDir.resolve("audit/deploy.log"));
        logger.record(AuditEvent.EventType.DEPLOY, "dev", "svc", "1.0", "user", null);

        assertThrows(UnsupportedOperationException.class,
                () -> logger.getSessionEvents().clear());
    }
}
