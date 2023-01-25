package de.bahmut.kindleproxy.model;

import java.util.Map;

public record RenderedChapter(
        String identifier,
        String bookIdentifier,
        String chapterTitle,
        String bookTitle,
        Map<Integer, Page> pages,
        int maxPage
) {

}