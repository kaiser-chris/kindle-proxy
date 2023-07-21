package de.bahmut.kindleproxy.handler.special;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LongParagraphHandler implements SpecialCaseHandler {
    @Override
    public boolean isTagSupported(String tag) {
        return "p".equalsIgnoreCase(tag);
    }

    @Override
    public Elements handleSpecialCase(Element paragraph) {
        if (paragraph.childrenSize() < 5) {
            return new Elements(paragraph);
        }
        final var replacement = new Elements();
        final List<Node> paragraphChildren = paragraph.childNodes();
        Element newParagraph = new Element("p");
        replacement.add(newParagraph);
        boolean wasBreak = false;
        for (final Node child : paragraphChildren) {
            if (child instanceof Element && ((Element) child).tagName().equalsIgnoreCase("br")) {
                wasBreak = true;
                continue;
            }
            if (wasBreak && newParagraph.childNodeSize() != 0) {
                newParagraph = new Element("p");
                replacement.add(newParagraph);
                wasBreak = false;
            }
            if (child instanceof Element) {
                newParagraph.appendChild(child);
            }
            if (child instanceof TextNode) {
                newParagraph.appendText(((TextNode) child).text());
            }
        }
        return replacement;
    }

}
