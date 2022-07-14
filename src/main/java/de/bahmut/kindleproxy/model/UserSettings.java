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

    public UserSettings() {
        this(24);
    }

    public int textSize() {
        return textSize;
    }

}
