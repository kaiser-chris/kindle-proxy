package de.bahmut.kindleproxy.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
public class UserSettings {

    private int textSize;

    private boolean footer;

    public UserSettings() {
        this(24, true);
    }

    public UserSettings(int textSize) {
        this(textSize, true);
    }

    public int textSize() {
        return textSize;
    }

    public boolean footer() {
        return footer;
    }

}
