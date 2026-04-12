package com.jarvis.deploy.audit;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Writes audit events to an append-only log file and keeps an in-memory list
 * for the duration of the CLI session.
 */
public class AuditLogger {

    private static final Logger LOG = Logger.getLogger(AuditLogger.class.getName());

    private final Path logFile;
    private final List<AuditEvent> sessionEvents = new ArrayList<>();

    public AuditLogger(Path logFile) {
        this.logFile = logFile;
        try {
            Files.createDirectories(logFile.getParent());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create audit log directory", e);
        }
    }

    /**
     * Records an audit event both in memory and on disk.
     */
    public AuditEvent record(AuditEvent.EventType type, String environment,
                              String service, String version,
                              String initiatedBy, String details) {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID().toString(),
                type, environment, service, version,
                initiatedBy, Instant.now(), details);

        sessionEvents.add(event);
        persist(event);
        LOG.info("Audit: " + event);
        return event;
    }

    private void persist(AuditEvent event) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                logFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(event.toString());
            writer.newLine();
        } catch (IOException e) {
            LOG.warning("Failed to persist audit event: " + e.getMessage());
        }
    }

    public List<AuditEvent> getSessionEvents() {
        return List.copyOf(sessionEvents);
    }

    public Path getLogFile() {
        return logFile;
    }
}
