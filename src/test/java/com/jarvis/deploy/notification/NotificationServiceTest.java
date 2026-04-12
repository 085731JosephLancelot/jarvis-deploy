package com.jarvis.deploy.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    private NotificationService service;

    @BeforeEach
    void setUp() {
        service = new NotificationService(NotificationLevel.WARNING);
    }

    @Test
    void constructor_throwsOnNullLevel() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationService(null));
    }

    @Test
    void send_storesEventAboveMinimumLevel() {
        service.notify("dep-1", "staging", "Deploy failed", NotificationLevel.ERROR);
        assertEquals(1, service.getHistory().size());
    }

    @Test
    void send_dropsEventBelowMinimumLevel() {
        service.notify("dep-2", "dev", "Deploy started", NotificationLevel.INFO);
        assertEquals(0, service.getHistory().size());
    }

    @Test
    void send_storesEventAtExactMinimumLevel() {
        service.notify("dep-3", "prod", "Health check warning", NotificationLevel.WARNING);
        assertEquals(1, service.getHistory().size());
    }

    @Test
    void send_throwsOnNullEvent() {
        assertThrows(IllegalArgumentException.class, () -> service.send(null));
    }

    @Test
    void getHistory_returnsUnmodifiableList() {
        service.notify("dep-4", "prod", "msg", NotificationLevel.ERROR);
        List<NotificationEvent> history = service.getHistory();
        assertThrows(UnsupportedOperationException.class, () -> history.add(null));
    }

    @Test
    void getHistoryFiltered_returnsOnlyMatchingLevel() {
        service.notify("dep-5", "prod", "warning msg",  NotificationLevel.WARNING);
        service.notify("dep-6", "prod", "critical msg", NotificationLevel.CRITICAL);
        service.notify("dep-7", "prod", "error msg",    NotificationLevel.ERROR);

        List<NotificationEvent> criticalAndAbove = service.getHistory(NotificationLevel.CRITICAL);
        assertEquals(1, criticalAndAbove.size());
        assertEquals(NotificationLevel.CRITICAL, criticalAndAbove.get(0).getLevel());
    }

    @Test
    void getMinimumLevel_returnsConfiguredLevel() {
        assertEquals(NotificationLevel.WARNING, service.getMinimumLevel());
    }
}
