package de.bahmut.kindleproxy.model;

public record UserSettings(
        int textSize
) {
    public UserSettings() {
        this(24);
    }
}
