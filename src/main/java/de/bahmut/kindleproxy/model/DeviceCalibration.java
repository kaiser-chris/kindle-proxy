package de.bahmut.kindleproxy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record DeviceCalibration(
        String userAgent,
        int width,
        int height,
        double defaultRatio,
        Map<Character, Double> characters
) {

    public double calculateCharacterWidth(final char character, final int fontSize) {
        return Optional.ofNullable(characters.get(character))
                .map(ratio -> (double) fontSize * ratio)
                .orElse((double) fontSize * defaultRatio);
    }

    @JsonIgnore
    public UUID getIdentifier() {
        return UUID.nameUUIDFromBytes(userAgent.getBytes(StandardCharsets.UTF_8));
    }

}
