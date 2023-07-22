package de.bahmut.kindleproxy.handler.special;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Break up a long paragraph that is seperated by line breaks
 * into multiple separate paragraphs.
 * There are some external texts that need this handling
 * to show correctly as multiple different pages:
 *  - Stray Cat Strut (RoyalRoad)
 */
@Component
public class LongParagraphHandler implements SpecialCaseHandler {

    @Override
    public boolean isTagSupported(String tag) {
        return "p".equalsIgnoreCase(tag);
    }

    @Override
    public Elements handleSpecialCase(Element paragraph) {
        // Only apply this special handling if there are at least 5 html elements
        // in paragraph (e.g. br, img, etc.)
        if (paragraph.childrenSize() < 5) {
            return new Elements(paragraph);
        }
        final var replacement = new Elements();
        final List<Node> paragraphChildren = paragraph.childNodes();
        Element newParagraph = new Element("p");
        replacement.add(newParagraph);
        boolean wasBreak = false;
        for (final Node child : paragraphChildren) {
            // If child element is a line break ignore it since it will lead to a new paragraph
            if (child instanceof Element && ((Element) child).tagName().equalsIgnoreCase("br")) {
                wasBreak = true;
                continue;
            }
            // If the last element was a line break start a new paragraph
            if (wasBreak && newParagraph.childNodeSize() != 0) {
                newParagraph = new Element("p");
                replacement.add(newParagraph);
                wasBreak = false;
            }
            // Add other html elements to new paragraph
            if (child instanceof Element) {
                newParagraph.appendChild(child);
            }
            // Add test to new paragraph
            if (child instanceof TextNode) {
                newParagraph.appendText(((TextNode) child).text());
            }
        }
        return replacement;
    }

}
