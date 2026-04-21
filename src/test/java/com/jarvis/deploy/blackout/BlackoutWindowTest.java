package com.jarvis.deploy.blackout;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BlackoutWindowTest {

    private static final Set<DayOfWeek> WEEKDAYS = EnumSet.of(
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);

    private BlackoutWindow buildWindow(LocalTime start, LocalTime end, Set<DayOfWeek> days, boolean enabled) {
        return new BlackoutWindow("bw-1", "Maintenance", start, end, days, "production", enabled);
    }

    @Test
    void isActive_returnsTrueWhenWithinWindow() {
        BlackoutWindow window = buildWindow(LocalTime.of(22, 0), LocalTime.of(23, 59), WEEKDAYS, true);
        LocalDateTime during = LocalDateTime.of(2024, 6, 3, 22, 30); // Monday
        assertTrue(window.isActive(during));
    }

    @Test
    void isActive_returnsFalseWhenOutsideTimeRange() {
        BlackoutWindow window = buildWindow(LocalTime.of(22, 0), LocalTime.of(23, 59), WEEKDAYS, true);
        LocalDateTime outside = LocalDateTime.of(2024, 6, 3, 10, 0); // Monday, daytime
        assertFalse(window.isActive(outside));
    }

    @Test
    void isActive_returnsFalseWhenDayNotIncluded() {
        BlackoutWindow window = buildWindow(LocalTime.of(22, 0), LocalTime.of(23, 59), WEEKDAYS, true);
        LocalDateTime weekend = LocalDateTime.of(2024, 6, 1, 22, 30); // Saturday
        assertFalse(window.isActive(weekend));
    }

    @Test
    void isActive_returnsFalseWhenDisabled() {
        BlackoutWindow window = buildWindow(LocalTime.of(22, 0), LocalTime.of(23, 59), WEEKDAYS, false);
        LocalDateTime during = LocalDateTime.of(2024, 6, 3, 22, 30);
        assertFalse(window.isActive(during));
    }

    @Test
    void isActive_handlesOvernightWindow() {
        BlackoutWindow window = buildWindow(LocalTime.of(23, 0), LocalTime.of(2, 0), WEEKDAYS, true);
        LocalDateTime afterMidnight = LocalDateTime.of(2024, 6, 4, 1, 30); // Tuesday 01:30
        assertTrue(window.isActive(afterMidnight));
        LocalDateTime beforeStart = LocalDateTime.of(2024, 6, 3, 22, 0); // Monday 22:00
        assertFalse(window.isActive(beforeStart));
    }

    @Test
    void appliesToEnvironment_matchesCaseInsensitive() {
        BlackoutWindow window = buildWindow(LocalTime.of(22, 0), LocalTime.of(23, 0), WEEKDAYS, true);
        assertTrue(window.appliesToEnvironment("production"));
        assertTrue(window.appliesToEnvironment("PRODUCTION"));
        assertFalse(window.appliesToEnvironment("staging"));
    }

    @Test
    void appliesToEnvironment_nullEnvMatchesAll() {
        BlackoutWindow window = new BlackoutWindow("bw-2", "Global", LocalTime.of(0, 0), LocalTime.of(1, 0),
                WEEKDAYS, null, true);
        assertTrue(window.appliesToEnvironment("staging"));
        assertTrue(window.appliesToEnvironment("production"));
    }

    @Test
    void constructor_throwsOnBlankId() {
        assertThrows(IllegalArgumentException.class, () ->
                new BlackoutWindow("", "name", LocalTime.NOON, LocalTime.MIDNIGHT, WEEKDAYS, null, true));
    }

    @Test
    void constructor_throwsOnEmptyDays() {
        assertThrows(IllegalArgumentException.class, () ->
                new BlackoutWindow("id", "name", LocalTime.NOON, LocalTime.MIDNIGHT, EnumSet.noneOf(DayOfWeek.class), null, true));
    }

    @Test
    void getters_returnExpectedValues() {
        BlackoutWindow window = buildWindow(LocalTime.of(8, 0), LocalTime.of(9, 0), WEEKDAYS, true);
        assertEquals("bw-1", window.getId());
        assertEquals("Maintenance", window.getName());
        assertEquals(LocalTime.of(8, 0), window.getStartTime());
        assertEquals(LocalTime.of(9, 0), window.getEndTime());
        assertEquals("production", window.getEnvironment());
        assertTrue(window.isEnabled());
    }
}
