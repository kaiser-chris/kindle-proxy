package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class ScriptCleaner implements ContentCleaner {

    @Override
    public Document clean(Document page) {
        page.select("script").remove();
        return page;
    }

}
