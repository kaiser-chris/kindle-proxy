package de.bahmut.kindleproxy.service.proxy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Content;
import de.bahmut.kindleproxy.service.ProxyService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Service
public class RoyalRoadProxy implements ProxyService {

    private static final String URL_PATTERN = "https://www.royalroad.com/fiction/%s/chapter/%s";
    private static final String HTML_CHAPTER_SELECTOR = "div.chapter-inner.chapter-content";
    private static final String HTML_CHAPTER_NAVIGATION_SELECTOR = ".btn.btn-primary.col-xs-12";

    @Override
    public Content getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final Document page;
        try {
            page = Jsoup.connect(String.format(URL_PATTERN, bookIdentifier, chapterIdentifier)).get();
        } catch (final IOException e) {
            throw new ProxyException("Could not retrieve chapter html", e);
        }
        final Elements links = page.select(HTML_CHAPTER_NAVIGATION_SELECTOR);
        final Element linkNext = links.stream().filter(element -> element.text().contains("Next") && element.text().contains("Chapter")).findAny().orElse(null);
        final Element linkPrevious = links.stream().filter(element -> element.text().contains("Previous") && element.text().contains("Chapter")).findAny().orElse(null);
        final Elements chapterContent = page.select(HTML_CHAPTER_SELECTOR);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter");
        checkProxyResult(chapterContent.size() == 0, "Could not find chapter");
        return new Content(
                page.title(),
                chapterContent.get(0).html(),
                getChapterIdentifier(linkNext),
                getChapterIdentifier(linkPrevious)
        );
    }

    private String getChapterIdentifier(final Element link) throws ProxyException {
        if (link == null) {
            // No Chapter link found
            return null;
        }
        if ("disabled".equalsIgnoreCase(link.attr("disabled"))) {
            // Chapter does not exist or is disabled
            return null;
        }
        final String chapterUrl = link.attr("href");
        final String[] urlParts = chapterUrl.split("/");
        if (urlParts.length < 2) {
            throw new ProxyException("Found an invalid chapter link: " + chapterUrl + " in element: " + link);
        }
        return urlParts[urlParts.length - 2] + "/" + urlParts[urlParts.length - 1];
    }

}
