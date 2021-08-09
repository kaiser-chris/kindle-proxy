package de.bahmut.kindleproxy.model;

import java.util.Map;

public record Chapter(
        String identifier,
        String bookIdentifier,
        String title,
        String body,
        String nextChapterIdentifier,
        String previousChapterIdentifier
) {

}
