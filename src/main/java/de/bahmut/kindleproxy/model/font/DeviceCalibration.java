package de.bahmut.kindleproxy.model.font;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.bahmut.kindleproxy.constant.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeviceCalibration {

    private String userAgent;

    private Device device;

    private double defaultRatio;

    private Map<Character, Double> characters = new HashMap<>();

    public double calculateCharacterWidth(final char character, final int fontSize) {
        return Optional.ofNullable(characters.get(character))
                .map(ratio -> (double) fontSize * ratio)
                .orElse((double) fontSize * defaultRatio);
    }

}
