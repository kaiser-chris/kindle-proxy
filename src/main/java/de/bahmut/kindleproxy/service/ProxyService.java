package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Content;

public interface ProxyService {

    Content getChapter(final String bookIdentifier, final String chapterIdentifier) throws ProxyException;

}
