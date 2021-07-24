package de.bahmut.kindleproxy.service.proxy;

import java.io.IOException;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Content;
import de.bahmut.kindleproxy.service.ProxyService;
import de.bahmut.kindleproxy.service.TemplateCacheService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Service
public class RoyalRoadProxy implements ProxyService {

    private static final String URL_PATTERN = "https://www.royalroad.com/fiction/14167/metaworld-chronicles/chapter/163621/chapter-3-pass-conceded";
    private static final String HTML_CHAPTER_SELECTOR = "div.chapter-inner.chapter-content";

    @Override
    public Content getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final Document page;
        try {
            page = Jsoup.connect(URL_PATTERN).get();
        } catch (IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
        Elements chapterContent = page.select(HTML_CHAPTER_SELECTOR);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter");
        checkProxyResult(chapterContent.size() == 0, "Could not find chapter");
        return new Content("Test Title", chapterContent.get(0).html());
    }

}
