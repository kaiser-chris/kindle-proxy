package de.bahmut.kindleproxy.service.proxy;

import de.bahmut.kindleproxy.exception.ProxyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractProxyImplementationTest {

    void testChapter(
            final ProxyService proxy,
            final String bookIdentifier,
            final String chapterIdentifier,
            final String bookTitle,
            final String chapterTitle,
            final String chapterContent
    ) throws ProxyException {
        var chapter = proxy.getChapter(bookIdentifier, chapterIdentifier);
        assertEquals(bookTitle, chapter.bookTitle(), "Book title does not equal '" + bookTitle + "'");
        assertEquals(chapterTitle, chapter.chapterTitle(), "Chapter title does not equal '" + chapterTitle + "'");
        assertTrue(chapter.htmlBody().contains(chapterContent), "Could not find string '" + chapterContent + "' in chapter");
    }

}
