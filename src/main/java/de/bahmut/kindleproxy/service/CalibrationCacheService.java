package de.bahmut.kindleproxy.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CalibrationCacheService {

    private static final double DEFAULT_RATIO = 0.9;

    private final ObjectMapper jsonMapper;
    private final Path cacheFolder;

    public CalibrationCacheService(
            final ObjectMapper jsonMapper,
            @Value("${proxy.calibration.cache-directory}") final Path cacheFolder
    ) throws CalibrationException {
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

    public DeviceCalibration cacheCalibration(final String agent, final Map<String, String> queryParameters) throws CalibrationException {
        final Map<Character, Double> characters = new HashMap<>();
        for (final Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (entry.getKey().length() != 1) {
                continue;
            }
            final char character = entry.getKey().charAt(0);
            final double ratio;
            try {
                ratio = Double.parseDouble(entry.getValue());
            } catch(final NumberFormatException e) {
                throw new CalibrationException("Could not parse character ratio for char " + character, e);
            }
            characters.put(character, ratio);
        }
        final double defaultRatio;
        if (characters.get('M') != null) {
            defaultRatio = characters.get('M');
        } else {
            defaultRatio = DEFAULT_RATIO;
        }
        final int width;
        final int height;
        try {
            width = Integer.parseInt(queryParameters.get("width"));
            height = Integer.parseInt(queryParameters.get("height"));
        } catch(final NumberFormatException e) {
            throw new CalibrationException("Could not parse resolution", e);
        }
        final var device = new DeviceCalibration(agent, width, height, defaultRatio, characters);
        final Path cacheFile = cacheFolder.resolve(device.getIdentifier() + ".json");
        try {
            Files.writeString(cacheFile, jsonMapper.writeValueAsString(device));
        } catch (final IOException e) {
            throw new CalibrationException("Could not cache calibration", e);
        }
        return device;
    }

    public Optional<DeviceCalibration> findCalibration(final String agent) throws IOException {
        final Path cacheFile = cacheFolder.resolve(UUID.nameUUIDFromBytes(agent.getBytes(StandardCharsets.UTF_8)) + ".json");
        if (Files.notExists(cacheFile)) {
            return Optional.empty();
        }
        return Optional.of(jsonMapper.readValue(Files.readAllBytes(cacheFile), DeviceCalibration.class));
    }

}
