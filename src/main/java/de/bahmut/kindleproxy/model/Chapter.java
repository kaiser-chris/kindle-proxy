package de.bahmut.kindleproxy.model;

public record Chapter(
        String identifier,
        String bookIdentifier,
        String title,
        String htmlBody,
        String nextChapterIdentifier,
        String previousChapterIdentifier
) {

}
