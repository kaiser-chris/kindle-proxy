package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.nodes.Document;

/**
 * Implement this interface to add a sanitization of
 * chapter content before it is used in page rendering.
 */
public interface ContentCleaner {

    Document clean(final Document page);

}
