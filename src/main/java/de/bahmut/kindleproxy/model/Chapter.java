package de.bahmut.kindleproxy.model;

public record Chapter(
        String identifier,
        String bookIdentifier,
        String chapterTitle,
        String bookTitle,
        String htmlBody,
        SiblingReference nextChapter,
        SiblingReference previousChapter
) {

}