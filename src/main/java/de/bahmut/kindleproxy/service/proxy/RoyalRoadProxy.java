package de.bahmut.kindleproxy.service.proxy;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.BookReference;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.ChapterReference;
import de.bahmut.kindleproxy.service.CacheService;
import de.bahmut.kindleproxy.service.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Log4j2
@Service
@RequiredArgsConstructor
public class RoyalRoadProxy implements ProxyService {

    private static final String BASE_URL = "https://www.royalroad.com/";

    private static final String URL_PATTERN_FAVORITES = BASE_URL + "profile/%s/favorites";
    private static final String HTML_SELECTOR_FAVORITE = "div.portlet-body div.mt-element-overlay";
    private static final String HTML_SELECTOR_FAVORITE_BOOK_LINK = ".btn.btn-default.btn-outline";
    private static final String HTML_SELECTOR_FAVORITE_BOOK_TITLE = "img.cover";

    private static final String URL_PATTERN_BOOK = BASE_URL + "fiction/%s";
    private static final String HTML_SELECTOR_BOOK_TITLE = "div.fic-title h1";
    private static final String HTML_SELECTOR_BOOK_CHAPTERS = "table#chapters td:first-child a";

    private static final String URL_PATTERN_CHAPTER = BASE_URL + "fiction/%s/chapter/%s";
    private static final String HTML_SELECTOR_CHAPTER_CONTENT = "div.chapter-inner.chapter-content";
    private static final String HTML_SELECTOR_CHAPTER_NAVIGATION = ".btn.btn-primary.col-xs-12";

    private final CacheService cacheService;

    @Value("${proxy.royal-road.favorites-user-id}")
    private String favoritesUserIdentifier;

    @Override
    public String getName() {
        return "Royal Road";
    }

    @Override
    public Chapter getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final String chapterUrl = String.format(URL_PATTERN_CHAPTER, bookIdentifier, chapterIdentifier);
        final Optional<Chapter> cachedChapter = cacheService.getCachedItem(chapterUrl, Chapter.class);
        if (cachedChapter.isPresent()) {
            log.debug("Using cached chapter: " + chapterUrl);
            return cachedChapter.get();
        }
        final Document page;
        try {
            page = Jsoup.connect(chapterUrl).get();
        } catch (final IOException e) {
            throw new ProxyException("Could not retrieve chapter html", e);
        }
        final Elements links = page.select(HTML_SELECTOR_CHAPTER_NAVIGATION);
        final Element linkNext = links.stream().filter(element -> element.text().contains("Next") && element.text().contains("Chapter")).findAny().orElse(null);
        final Element linkPrevious = links.stream().filter(element -> element.text().contains("Previous") && element.text().contains("Chapter")).findAny().orElse(null);
        final Elements chapterContent = page.select(HTML_SELECTOR_CHAPTER_CONTENT);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter");
        checkProxyResult(chapterContent.size() == 0, "Could not find chapter");
        final Chapter chapter = new Chapter(
                chapterIdentifier,
                bookIdentifier,
                page.title(),
                chapterContent.get(0).html(),
                getIdentifier(linkNext),
                getIdentifier(linkPrevious)
        );
        cacheService.addItemToCache(chapterUrl, chapter);
        return chapter;
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
    public List<BookReference> getBooks() throws ProxyException {
        if (StringUtils.isBlank(favoritesUserIdentifier)) {
            //TODO
            return List.of();
        }
        final Document page;
        try {
            page = Jsoup.connect(String.format(URL_PATTERN_FAVORITES, favoritesUserIdentifier)).get();
        } catch (final IOException e) {
            throw new ProxyException("Could not retrieve favorites html", e);
        }
        final Elements favorites = page.select(HTML_SELECTOR_FAVORITE);
        final List<BookReference> books = new LinkedList<>();
        for (final Element favorite : favorites) {
            final String title = favorite.select(HTML_SELECTOR_FAVORITE_BOOK_TITLE).stream().findAny().map(element -> element.attr("alt")).orElse(null);
            final String identifier = favorite.select(HTML_SELECTOR_FAVORITE_BOOK_LINK).stream().map(this::getIdentifier).findAny().orElse(null);
            if (identifier == null) {
                continue;
            }
            books.add(new BookReference(identifier, Objects.requireNonNullElse(title, identifier)));
        }
        return books;
    }

    private ChapterReference getChapterReference(final Element link) {
        return new ChapterReference(
                getIdentifier(link),
                link.text().strip()
        );
    }

    private String getIdentifier(final Element link) {
        if (link == null) {
            log.warn("No Chapter link found");
            return null;
        }
        if ("disabled".equalsIgnoreCase(link.attr("disabled"))) {
            log.trace("Chapter does not exist or is disabled: " + link);
            return null;
        }
        final String url = link.attr("href");
        final String[] urlParts = url.split("/");
        if (urlParts.length < 2) {
            log.warn("Found an invalid identifier link: " + url + " in element: " + link);
            return null;
        }
        return urlParts[urlParts.length - 2] + "/" + urlParts[urlParts.length - 1];
    }

}
