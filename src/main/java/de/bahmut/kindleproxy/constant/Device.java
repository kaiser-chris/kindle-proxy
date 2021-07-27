package de.bahmut.kindleproxy.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Device {

    UNKNOWN("Unknown Device", -1, -1, 24),
    KINDLE_PAPERWHITE_2("Kindle Paperwhite", 740, 890, 24);

    private final String name;
    private final int width;
    private final int height;
    private final int fontSize;

}
