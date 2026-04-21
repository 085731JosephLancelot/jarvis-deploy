package com.jarvis.deploy.compress;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactCompressorTest {

    private ArtifactCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = new ArtifactCompressor(CompressionStrategy.GZIP);
    }

    @Test
    void compressWithDefaultStrategy_createsRecord() {
        CompressionRecord record = compressor.compress("artifact-1", 10_000L);

        assertNotNull(record);
        assertEquals("artifact-1", record.getArtifactId());
        assertEquals(CompressionStrategy.GZIP, record.getStrategy());
        assertEquals(10_000L, record.getOriginalSizeBytes());
        assertTrue(record.getCompressedSizeBytes() < record.getOriginalSizeBytes());
        assertTrue(record.compressionRatio() < 1.0);
        assertTrue(record.savedBytes() > 0);
    }

    @Test
    void compressWithExplicitStrategy_usesGivenStrategy() {
        CompressionRecord record = compressor.compress("artifact-2", 20_000L, CompressionStrategy.ZSTD);

        assertEquals(CompressionStrategy.ZSTD, record.getStrategy());
        assertTrue(record.compressionRatio() <= 0.56);
    }

    @Test
    void compressWithNoneStrategy_retainsSameSize() {
        CompressionRecord record = compressor.compress("artifact-3", 5_000L, CompressionStrategy.NONE);

        assertEquals(5_000L, record.getOriginalSizeBytes());
        assertEquals(5_000L, record.getCompressedSizeBytes());
        assertEquals(1.0, record.compressionRatio(), 0.001);
        assertEquals(0L, record.savedBytes());
    }

    @Test
    void hasRecord_returnsTrueAfterCompression() {
        assertFalse(compressor.hasRecord("artifact-4"));
        compressor.compress("artifact-4", 1_000L);
        assertTrue(compressor.hasRecord("artifact-4"));
    }

    @Test
    void getRecord_returnsStoredRecord() {
        compressor.compress("artifact-5", 8_000L);
        CompressionRecord record = compressor.getRecord("artifact-5");

        assertNotNull(record);
        assertEquals("artifact-5", record.getArtifactId());
    }

    @Test
    void removeRecord_deletesExistingRecord() {
        compressor.compress("artifact-6", 3_000L);
        assertTrue(compressor.removeRecord("artifact-6"));
        assertFalse(compressor.hasRecord("artifact-6"));
    }

    @Test
    void removeRecord_returnsFalseWhenNotFound() {
        assertFalse(compressor.removeRecord("nonexistent"));
    }

    @Test
    void compress_throwsOnNegativeSize() {
        assertThrows(IllegalArgumentException.class,
                () -> compressor.compress("artifact-7", -1L));
    }

    @Test
    void compress_throwsOnNullArtifactId() {
        assertThrows(NullPointerException.class,
                () -> compressor.compress(null, 1_000L));
    }

    @Test
    void compressionRecord_zeroOriginalSizeRatioIsOne() {
        CompressionRecord record = compressor.compress("artifact-8", 0L, CompressionStrategy.NONE);
        assertEquals(1.0, record.compressionRatio(), 0.001);
    }
}
