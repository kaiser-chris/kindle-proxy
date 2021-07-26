package de.bahmut.kindleproxy.service.proxy;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.BookReference;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.ChapterReference;
import de.bahmut.kindleproxy.service.ProxyService;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Log4j2
@Service
public class RoyalRoadProxy implements ProxyService {

    private static final String URL_PATTERN_BOOK = "https://www.royalroad.com/fiction/%s";
    private static final String HTML_SELECTOR_BOOK_TITLE = "div.fic-title h1";
    private static final String HTML_SELECTOR_BOOK_CHAPTERS = "table#chapters td:first-child a";

    private static final String URL_PATTERN_CHAPTER = "https://www.royalroad.com/fiction/%s/chapter/%s";
    private static final String HTML_SELECTOR_CHAPTER_CONTENT = "div.chapter-inner.chapter-content";
    private static final String HTML_SELECTOR_CHAPTER_NAVIGATION = ".btn.btn-primary.col-xs-12";

    @Override
    public String getName() {
        return "Royal Road";
    }

    @Override
    public Chapter getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final Document page;
        try {
            page = Jsoup.connect(String.format(URL_PATTERN_CHAPTER, bookIdentifier, chapterIdentifier)).get();
        } catch (final IOException e) {
            throw new ProxyException("Could not retrieve chapter html", e);
        }
        final Elements links = page.select(HTML_SELECTOR_CHAPTER_NAVIGATION);
        final Element linkNext = links.stream().filter(element -> element.text().contains("Next") && element.text().contains("Chapter")).findAny().orElse(null);
        final Element linkPrevious = links.stream().filter(element -> element.text().contains("Previous") && element.text().contains("Chapter")).findAny().orElse(null);
        final Elements chapterContent = page.select(HTML_SELECTOR_CHAPTER_CONTENT);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter");
        checkProxyResult(chapterContent.size() == 0, "Could not find chapter");
        return new Chapter(
                chapterIdentifier,
                bookIdentifier,
                page.title(),
                chapterContent.get(0).html(),
                getChapterIdentifier(linkNext),
                getChapterIdentifier(linkPrevious)
        );
    }

    @Override
    public Book getBook(String bookIdentifier) throws ProxyException {
        final Document page;
        try {
            page = Jsoup.connect(String.format(URL_PATTERN_BOOK, bookIdentifier)).get();
        } catch (final IOException e) {
            throw new ProxyException("Could not retrieve book html", e);
        }
        final String name = page.select(HTML_SELECTOR_BOOK_TITLE).stream().findAny().map(Element::text).orElse(bookIdentifier);
        final List<ChapterReference> chapters = page.select(HTML_SELECTOR_BOOK_CHAPTERS).stream()
                .map(this::getChapterReference)
                .collect(Collectors.toList());
        return new Book(
                bookIdentifier,
                name,
                chapters
        );
    }

    @Override
    public List<BookReference> getBooks() {
        //TODO
        return List.of(new BookReference("14167/metaworld-chronicles", "Metaworld Chronicles"));
    }

    private ChapterReference getChapterReference(final Element link) {
        return new ChapterReference(
                getChapterIdentifier(link),
                link.text().strip()
        );
    }

    private String getChapterIdentifier(final Element link) {
        if (link == null) {
            log.warn("No Chapter link found");
            return null;
        }
        if ("disabled".equalsIgnoreCase(link.attr("disabled"))) {
            log.trace("Chapter does not exist or is disabled: " + link);
            return null;
        }
        final String chapterUrl = link.attr("href");
        final String[] urlParts = chapterUrl.split("/");
        if (urlParts.length < 2) {
            log.warn("Found an invalid chapter link: " + chapterUrl + " in element: " + link);
            return null;
        }
        return urlParts[urlParts.length - 2] + "/" + urlParts[urlParts.length - 1];
    }

}
