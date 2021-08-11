package de.bahmut.kindleproxy.service.proxy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.Reference;
import de.bahmut.kindleproxy.model.Chapter;

public interface ProxyService {

    default UUID getId() {
        return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_8));
    }

    String getName();

    Chapter getChapter(final String bookIdentifier, final String chapterIdentifier) throws ProxyException;

    Book getBook(final String bookIdentifier) throws ProxyException;

    List<Reference> getBooks() throws ProxyException;

}
