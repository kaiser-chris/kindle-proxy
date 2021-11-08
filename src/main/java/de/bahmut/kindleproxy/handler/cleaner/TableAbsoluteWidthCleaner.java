package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

/**
 * Since the proxy supports multiple device sizes
 * absolute width attributes should be removed.
 */
@Component
public class TableAbsoluteWidthCleaner implements ContentCleaner {

    private static final String HTML_SELECTOR_TABLE = "table, thead, tbody, tr, th, td";
    private static final String ATTRIBUTE_WIDTH = "width";

    @Override
    public Document clean(Document page) {
        for (final Element element : page.select(HTML_SELECTOR_TABLE)) {
            final String width = element.attr(ATTRIBUTE_WIDTH);
            if (width.contains("%")) {
                // Only remove absolute width attributes
                continue;
            }
            element.removeAttr(ATTRIBUTE_WIDTH);
        }
        return page;
    }

}
