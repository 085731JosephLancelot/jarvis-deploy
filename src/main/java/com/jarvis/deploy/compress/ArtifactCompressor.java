package com.jarvis.deploy.compress;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Manages compression and decompression of deployment artifacts.
 * Tracks compression metadata per artifact ID.
 */
public class ArtifactCompressor {

    private static final Logger LOGGER = Logger.getLogger(ArtifactCompressor.class.getName());

    private final Map<String, CompressionRecord> records = new HashMap<>();
    private final CompressionStrategy defaultStrategy;

    public ArtifactCompressor(CompressionStrategy defaultStrategy) {
        this.defaultStrategy = Objects.requireNonNull(defaultStrategy, "defaultStrategy must not be null");
    }

    /**
     * Compresses an artifact using the default strategy.
     *
     * @param artifactId unique artifact identifier
     * @param sizeBytes  original size in bytes
     * @return the resulting CompressionRecord
     */
    public CompressionRecord compress(String artifactId, long sizeBytes) {
        return compress(artifactId, sizeBytes, defaultStrategy);
    }

    /**
     * Compresses an artifact using the specified strategy.
     *
     * @param artifactId unique artifact identifier
     * @param sizeBytes  original size in bytes
     * @param strategy   compression strategy to apply
     * @return the resulting CompressionRecord
     */
    public CompressionRecord compress(String artifactId, long sizeBytes, CompressionStrategy strategy) {
        Objects.requireNonNull(artifactId, "artifactId must not be null");
        Objects.requireNonNull(strategy, "strategy must not be null");
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("sizeBytes must be non-negative");
        }

        long compressedSize = strategy.isCompressed() ? simulateCompression(sizeBytes, strategy) : sizeBytes;
        CompressionRecord record = new CompressionRecord(artifactId, strategy, sizeBytes, compressedSize);
        records.put(artifactId, record);
        LOGGER.info(String.format("Compressed artifact '%s' using %s: %d -> %d bytes",
                artifactId, strategy, sizeBytes, compressedSize));
        return record;
    }

    /**
     * Retrieves the compression record for the given artifact, if present.
     */
    public CompressionRecord getRecord(String artifactId) {
        return records.get(artifactId);
    }

    /**
     * Returns true if a compression record exists for the given artifact.
     */
    public boolean hasRecord(String artifactId) {
        return records.containsKey(artifactId);
    }

    /**
     * Removes the compression record for the given artifact.
     */
    public boolean removeRecord(String artifactId) {
        return records.remove(artifactId) != null;
    }

    /** Simulates compressed size based on strategy ratio. */
    private long simulateCompression(long original, CompressionStrategy strategy) {
        double ratio = switch (strategy) {
            case GZIP -> 0.60;
            case ZIP  -> 0.65;
            case ZSTD -> 0.55;
            default   -> 1.0;
        };
        return Math.max(1L, (long) (original * ratio));
    }
}
