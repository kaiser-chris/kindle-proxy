package de.bahmut.kindleproxy.constant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Device {

    UNKNOWN("Unknown Device", -1, -1, 16, "unknown.css"),
    KINDLE_PAPERWHITE("Kindle Paperwhite", 740, 890, 24, "kindle-paperwhite.css");

    private final String name;
    private final int width;
    private final int height;
    private final int fontSize;
    private final String cssFile;

}
