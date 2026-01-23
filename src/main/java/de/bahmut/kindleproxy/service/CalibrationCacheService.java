package de.bahmut.kindleproxy.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class CalibrationCacheService {

    private final ObjectMapper jsonMapper;
    private final Path cacheFolder;
    private final CacheService cacheService;

    public CalibrationCacheService(
            final ObjectMapper jsonMapper,
            final CacheService cacheService,
            @Value("${proxy.calibration.cache-directory}") final Path cacheFolder
    ) throws CalibrationException {
        this.cacheService = cacheService;
        this.jsonMapper = jsonMapper;
        this.cacheFolder = cacheFolder;
        if (Files.notExists(cacheFolder)) {
            try {
                Files.createDirectories(cacheFolder);
            } catch (final IOException e) {
                throw new CalibrationException("Could not create cache directory", e);
            }
        }
    }

    public void cacheCalibration(
            final UUID userIdentifier,
            final DeviceCalibration calibration
    ) throws CalibrationException {
        final Path cacheFile = cacheFolder.resolve(userIdentifier + ".json");
        try {
            Files.writeString(cacheFile, jsonMapper.writeValueAsString(calibration));
        } catch (final IOException e) {
            throw new CalibrationException("Could not cache calibration", e);
        }
        cacheService.invalidItemsByConditionIdentifier(userIdentifier.toString());
    }

    public Optional<DeviceCalibration> findCalibration(final UUID userIdentifier) throws CalibrationException {
        final Path cacheFile = cacheFolder.resolve(userIdentifier + ".json");
        if (Files.notExists(cacheFile)) {
            return Optional.empty();
        }
        try {
            return Optional.of(jsonMapper.readValue(Files.readAllBytes(cacheFile), DeviceCalibration.class));
        } catch (IOException e) {
            throw new CalibrationException("Could not read calibration from disk", e);
        }
    }

    public void deleteCalibration(final UUID userIdentifier) throws CalibrationException {
        final Path cacheFile = cacheFolder.resolve(userIdentifier + ".json");
        if (Files.notExists(cacheFile)) {
            return;
        }
        try {
            Files.delete(cacheFile);
        } catch (IOException e) {
            throw new CalibrationException("Could not delete calibration from disk", e);
        }
    }

}
