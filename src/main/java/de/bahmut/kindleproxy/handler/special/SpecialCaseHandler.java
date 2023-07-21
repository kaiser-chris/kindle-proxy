package de.bahmut.kindleproxy.handler.special;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Implement this interface to provide special handling
 * for specific html elements
 */
public interface SpecialCaseHandler {

    boolean isTagSupported(final String tag);

    Elements handleSpecialCase(
            final Element element
    );

}
