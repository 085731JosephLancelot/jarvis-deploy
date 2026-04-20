package com.jarvis.deploy.ratelimit;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token-bucket style rate limiter for controlling deployment frequency
 * per environment or per user/caller identity.
 */
public class RateLimiter {

    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, Deque<Instant>> requestWindows = new ConcurrentHashMap<>();

    public RateLimiter(int maxRequests, long windowSeconds) {
        if (maxRequests <= 0) throw new IllegalArgumentException("maxRequests must be positive");
        if (windowSeconds <= 0) throw new IllegalArgumentException("windowSeconds must be positive");
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    /**
     * Attempts to acquire a permit for the given key.
     *
     * @param key identifier (e.g. environment name or user id)
     * @return true if the request is allowed, false if rate limit exceeded
     */
    public synchronized boolean tryAcquire(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        Instant now = Instant.now();
        Deque<Instant> timestamps = requestWindows.computeIfAbsent(key, k -> new ArrayDeque<>());
        evictExpired(timestamps, now);
        if (timestamps.size() < maxRequests) {
            timestamps.addLast(now);
            return true;
        }
        return false;
    }

    /**
     * Returns the number of remaining permits for the given key in the current window.
     */
    public synchronized int remainingPermits(String key) {
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key must not be blank");
        Deque<Instant> timestamps = requestWindows.getOrDefault(key, new ArrayDeque<>());
        evictExpired(timestamps, Instant.now());
        return Math.max(0, maxRequests - timestamps.size());
    }

    /**
     * Resets the rate limit window for the given key.
     */
    public synchronized void reset(String key) {
        requestWindows.remove(key);
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getWindowSeconds() {
        return windowSeconds;
    }

    private void evictExpired(Deque<Instant> timestamps, Instant now) {
        Instant cutoff = now.minusSeconds(windowSeconds);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
    }
}
