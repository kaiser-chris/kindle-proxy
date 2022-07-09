package de.bahmut.kindleproxy.model;

import java.util.Map;
import java.util.Optional;

public record DeviceCalibration(
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

}
