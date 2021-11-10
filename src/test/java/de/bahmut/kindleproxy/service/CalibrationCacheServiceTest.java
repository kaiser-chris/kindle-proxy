package de.bahmut.kindleproxy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalibrationCacheServiceTest {

    private static final String TEST_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36";
    private static final int TEST_WIDTH = 1920;
    private static final int TEST_HEIGHT = 947;

    private static Path testBaseCacheFolder;

    @BeforeAll
    static void setUpBaseCache() throws IOException {
        testBaseCacheFolder = Files.createTempDirectory("CalibrationCacheServiceTest");
    }

    @AfterAll
    @SuppressWarnings("unused")
    static void markCacheForDeletion() throws IOException {
        testBaseCacheFolder.toFile().deleteOnExit();
        Files.walkFileTree(testBaseCacheFolder, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                file.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                dir.toFile().deleteOnExit();
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private CalibrationCacheService service;
    private Path cacheFolder;

    @BeforeEach
    void setUp() throws CalibrationException {
        cacheFolder = testBaseCacheFolder.resolve(UUID.randomUUID().toString());
        service = new CalibrationCacheService(new ObjectMapper(), cacheFolder);
    }

    @DisplayName("Test whether caching works with valid query parameters")
    @Test
    void testValidCaching() throws CalibrationException {
        final DeviceCalibration calibration = service.cacheCalibration(TEST_AGENT, createValidQueryParameters());
        assertEquals(TEST_AGENT, calibration.userAgent());
        assertEquals(TEST_WIDTH, calibration.width());
        assertEquals(TEST_HEIGHT, calibration.height());
        assertTrue(Files.exists(cacheFolder.resolve(calibration.getIdentifier() + ".json")));
    }

    @DisplayName("Test whether an invalid ratio is handled properly")
    @Test
    void testInvalidRatio() {
        final Map<String, String> parameters = createValidQueryParameters();
        parameters.put("a", "definitely not a number");
        assertThrows(CalibrationException.class, () -> service.cacheCalibration(TEST_AGENT, parameters));
    }

    @DisplayName("Test whether an invalid width value is handled properly")
    @Test
    void testInvalidWidth() {
        final Map<String, String> parameters = createValidQueryParameters();
        parameters.put("width", "definitely not a number");
        assertThrows(CalibrationException.class, () -> service.cacheCalibration(TEST_AGENT, parameters));
    }

    @DisplayName("Test whether cached calibration can be found and loaded again")
    @Test
    void findCalibration() throws CalibrationException, IOException {
        final DeviceCalibration createdCalibration = service.cacheCalibration(TEST_AGENT, createValidQueryParameters());
        final Optional<DeviceCalibration> foundCalibration = service.findCalibration(TEST_AGENT);
        assertTrue(foundCalibration.isPresent());
        assertEquals(createdCalibration.getIdentifier(), foundCalibration.get().getIdentifier());
    }

    @DisplayName("Test whether a not exiting calibration is handled properly")
    @Test
    void findNotExistingCalibration() throws IOException {
        final Optional<DeviceCalibration> foundCalibration = service.findCalibration(TEST_AGENT);
        assertTrue(foundCalibration.isEmpty());
    }

    /**
     * Data was taken from a real request.
     *
     * @return Valid map of query parameters.
     */
    private Map<String, String> createValidQueryParameters() {
        final Map<String, String> parameters = new HashMap<>();
        parameters.put("redirect", "/render/proxy/8bf6b58e-6630-38cb-ae0e-7e73f29716de/?book=36299%2Fbeneath-the-dragoneye-moons&chapter=562716%2Fchapter-14-decisions-i&page=1");
        parameters.put("calibrated", "true");
        parameters.put("a", "0.56");
        parameters.put("A", "0.76");
        parameters.put("b", "0.64");
        parameters.put("B", "0.68");
        parameters.put("c", "0.52");
        parameters.put("C", "0.72");
        parameters.put("d", "0.64");
        parameters.put("D", "0.76");
        parameters.put("e", "0.52");
        parameters.put("E", "0.64");
        parameters.put("f", "0.4");
        parameters.put("F", "0.64");
        parameters.put("g", "0.56");
        parameters.put("G", "0.76");
        parameters.put("h", "0.68");
        parameters.put("H", "0.84");
        parameters.put("i", "0.36");
        parameters.put("I", "0.44");
        parameters.put("j", "0.32");
        parameters.put("J", "0.56");
        parameters.put("k", "0.64");
        parameters.put("K", "0.76");
        parameters.put("l", "0.36");
        parameters.put("L", "0.64");
        parameters.put("m", "0.96");
        parameters.put("M", "1");
        parameters.put("o", "0.6");
        parameters.put("O", "0.76");
        parameters.put("p", "0.64");
        parameters.put("P", "0.6");
        parameters.put("q", "0.6");
        parameters.put("Q", "0.76");
        parameters.put("r", "0.48");
        parameters.put("R", "0.72");
        parameters.put("s", "0.48");
        parameters.put("S", "0.6");
        parameters.put("t", "0.44");
        parameters.put("T", "0.76");
        parameters.put("u", "0.64");
        parameters.put("U", "0.8");
        parameters.put("v", "0.6");
        parameters.put("V", "0.76");
        parameters.put("w", "0.88");
        parameters.put("W", "1.08");
        parameters.put("y", "0.6");
        parameters.put("Y", "0.72");
        parameters.put("z", "0.52");
        parameters.put("Z", "0.68");
        parameters.put(" ", "1");
        parameters.put(".", "0.36");
        parameters.put(",", "0.36");
        parameters.put("\"", "0.4");
        parameters.put("'", "0.28");
        parameters.put("!", "0.36");
        parameters.put("?", "0.56");
        parameters.put("“", "0.56");
        parameters.put("”", "0.56");
        parameters.put("◼", "0.84");
        parameters.put("width", String.valueOf(TEST_WIDTH));
        parameters.put("height", String.valueOf(TEST_HEIGHT));
        return parameters;
    }

}