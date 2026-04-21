package com.jarvis.deploy.blackout;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a blackout window during which deployments are prohibited.
 */
public class BlackoutWindow {

    private final String id;
    private final String name;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Set<DayOfWeek> activeDays;
    private final String environment;
    private final boolean enabled;

    public BlackoutWindow(String id, String name, LocalTime startTime, LocalTime endTime,
                          Set<DayOfWeek> activeDays, String environment, boolean enabled) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Blackout window id must not be blank");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Blackout window name must not be blank");
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");
        if (activeDays == null || activeDays.isEmpty()) throw new IllegalArgumentException("activeDays must not be empty");
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activeDays = Collections.unmodifiableSet(EnumSet.copyOf(activeDays));
        this.environment = environment;
        this.enabled = enabled;
    }

    public boolean isActive(LocalDateTime dateTime) {
        if (!enabled) return false;
        DayOfWeek day = dateTime.getDayOfWeek();
        if (!activeDays.contains(day)) return false;
        LocalTime time = dateTime.toLocalTime();
        if (startTime.isBefore(endTime)) {
            return !time.isBefore(startTime) && time.isBefore(endTime);
        } else {
            // overnight window e.g. 22:00 - 02:00
            return !time.isBefore(startTime) || time.isBefore(endTime);
        }
    }

    public boolean appliesToEnvironment(String env) {
        return environment == null || environment.equalsIgnoreCase(env);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public Set<DayOfWeek> getActiveDays() { return activeDays; }
    public String getEnvironment() { return environment; }
    public boolean isEnabled() { return enabled; }

    @Override
    public String toString() {
        return String.format("BlackoutWindow{id='%s', name='%s', start=%s, end=%s, days=%s, env=%s, enabled=%b}",
                id, name, startTime, endTime, activeDays, environment, enabled);
    }
}
