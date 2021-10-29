package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class TextColorCleaner implements ContentCleaner {

    @Override
    public Document clean(Document page) {
        for (final Element element : page.getAllElements()) {
            element.removeAttr("style");
            element.removeAttr("color");
        }
        return page;
    }

}
