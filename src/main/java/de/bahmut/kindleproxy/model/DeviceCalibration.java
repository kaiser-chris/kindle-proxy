package de.bahmut.kindleproxy.model;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCalibration {

    private String userAgent;

    private int width;

    private int height;

    private double defaultRatio;

    private Map<Character, Double> characters = new HashMap<>();

    public double calculateCharacterWidth(final char character, final int fontSize) {
        return Optional.ofNullable(characters.get(character))
                .map(ratio -> (double) fontSize * ratio)
                .orElse((double) fontSize * defaultRatio);
    }

    public UUID getIdentifier() {
        return UUID.nameUUIDFromBytes(userAgent.getBytes(StandardCharsets.UTF_8));
    }

}
