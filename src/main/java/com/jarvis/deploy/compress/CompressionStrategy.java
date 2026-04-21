package com.jarvis.deploy.compress;

/**
 * Defines the compression strategy to apply to deployment artifacts.
 */
public enum CompressionStrategy {

    /** No compression applied. */
    NONE,

    /** GZIP compression. */
    GZIP,

    /** ZIP compression. */
    ZIP,

    /** ZSTD (Zstandard) compression for high-performance use cases. */
    ZSTD;

    /**
     * Returns true if this strategy actually performs compression.
     */
    public boolean isCompressed() {
        return this != NONE;
    }
}
