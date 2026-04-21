package com.jarvis.deploy.compress;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable record capturing the result of a compression operation on a deployment artifact.
 */
public final class CompressionRecord {

    private final String artifactId;
    private final CompressionStrategy strategy;
    private final long originalSizeBytes;
    private final long compressedSizeBytes;
    private final Instant compressedAt;

    public CompressionRecord(String artifactId,
                             CompressionStrategy strategy,
                             long originalSizeBytes,
                             long compressedSizeBytes) {
        this.artifactId = Objects.requireNonNull(artifactId, "artifactId must not be null");
        this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
        if (originalSizeBytes < 0 || compressedSizeBytes < 0) {
            throw new IllegalArgumentException("Size values must be non-negative");
        }
        this.originalSizeBytes = originalSizeBytes;
        this.compressedSizeBytes = compressedSizeBytes;
        this.compressedAt = Instant.now();
    }

    public String getArtifactId() { return artifactId; }
    public CompressionStrategy getStrategy() { return strategy; }
    public long getOriginalSizeBytes() { return originalSizeBytes; }
    public long getCompressedSizeBytes() { return compressedSizeBytes; }
    public Instant getCompressedAt() { return compressedAt; }

    /**
     * Returns the compression ratio as a value between 0.0 and 1.0.
     * A ratio of 0.6 means the artifact was compressed to 60% of its original size.
     * Returns 1.0 if the original size is zero.
     */
    public double compressionRatio() {
        if (originalSizeBytes == 0) return 1.0;
        return (double) compressedSizeBytes / originalSizeBytes;
    }

    /**
     * Returns the space saved in bytes.
     */
    public long savedBytes() {
        return originalSizeBytes - compressedSizeBytes;
    }

    @Override
    public String toString() {
        return String.format("CompressionRecord{artifactId='%s', strategy=%s, original=%d, compressed=%d, ratio=%.2f}",
                artifactId, strategy, originalSizeBytes, compressedSizeBytes, compressionRatio());
    }
}
