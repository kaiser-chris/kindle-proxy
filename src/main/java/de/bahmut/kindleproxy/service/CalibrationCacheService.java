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
import de.bahmut.kindleproxy.model.font.DeviceCalibration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalibrationCacheService {

    private static final Path CACHE_FOLDER = Path.of("D:\\Development\\kindle-cache");

    private static final double DEFAULT_RATIO = 0.9;

    private final DeviceDetectionService deviceDetectionService;
    private final ObjectMapper jsonMapper;

    public void cacheCalibration(final String agent, final Map<String, String> queryParameters) {
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
                continue;
            }
            characters.put(character, ratio);
        }
        final double defaultRatio;
        if (characters.get('M') != null) {
            defaultRatio = characters.get('M');
        } else {
            defaultRatio = DEFAULT_RATIO;
        }
        final var device = new DeviceCalibration(agent, deviceDetectionService.detectDevice(), defaultRatio, characters);
        final Path cacheFile = CACHE_FOLDER.resolve(UUID.nameUUIDFromBytes(device.getUserAgent().getBytes(StandardCharsets.UTF_8)) + ".json");
        try {
            Files.writeString(cacheFile, jsonMapper.writeValueAsString(device));
        } catch (final IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }

    public Optional<DeviceCalibration> findCalibration(final String agent) throws IOException {
        final Path cacheFile = CACHE_FOLDER.resolve(UUID.nameUUIDFromBytes(agent.getBytes(StandardCharsets.UTF_8)) + ".json");
        if (Files.notExists(cacheFile)) {
            return Optional.empty();
        }
        return Optional.of(jsonMapper.readValue(Files.readAllBytes(cacheFile), DeviceCalibration.class));
    }

}
