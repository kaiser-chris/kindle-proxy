package de.bahmut.kindleproxy.service.proxy;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.Reference;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.SiblingReference;
import de.bahmut.kindleproxy.service.CacheService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Log4j2
@Service
public class RoyalRoadProxy extends CachedWebProxyService {

    private static final String BASE_URL = "https://www.royalroad.com/";

    private static final String URL_BEST_RATED = BASE_URL + "fictions/best-rated";
    private static final String HTML_SELECTOR_BEST_RATED_LINK = "h2.fiction-title a";

    private static final String URL_PATTERN_FAVORITES = BASE_URL + "profile/%s/favorites?page=%s";
    private static final String HTML_SELECTOR_FAVORITE = "div.portlet-body div.mt-element-overlay";
    private static final String HTML_SELECTOR_FAVORITE_BOOK_LINK = ".btn.btn-default.btn-outline";
    private static final String HTML_SELECTOR_FAVORITE_BOOK_TITLE = "img.cover";

    private static final String URL_PATTERN_BOOK = BASE_URL + "fiction/%s";
    private static final String HTML_SELECTOR_BOOK_TITLE = "div.fic-title h1";
    private static final String HTML_SELECTOR_BOOK_CHAPTERS = "table#chapters td:first-child a";

    private static final String URL_PATTERN_CHAPTER = BASE_URL + "fiction/%s/chapter/%s";
    private static final String HTML_SELECTOR_CHAPTER_TITLE = "div.fic-header h1";
    private static final String HTML_SELECTOR_CHAPTER_BOOK_TITLE = "div.fic-header h2";
    private static final String HTML_SELECTOR_CHAPTER_CONTENT = "div.chapter-inner.chapter-content";
    private static final String HTML_SELECTOR_CHAPTER_NAVIGATION = ".btn.btn-primary.col-xs-12";

    @Value("${proxy.royal-road.favorites-user-id}")
    private String favoritesUserIdentifier;

    public RoyalRoadProxy(final CacheService cacheService) {
        super(cacheService);
    }

    @Override
    public String getName() {
        return "Royal Road";
    }

    @Override
    public Chapter getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final String chapterUrl = String.format(URL_PATTERN_CHAPTER, bookIdentifier, chapterIdentifier);
        final Document page = retrieveDocument(chapterUrl);
        final String chapterName = page.select(HTML_SELECTOR_CHAPTER_TITLE).stream().findAny().map(Element::text).orElse(chapterIdentifier);
        final String bookName = page.select(HTML_SELECTOR_CHAPTER_BOOK_TITLE).stream().findAny().map(Element::text).orElse(bookIdentifier);
        final Elements links = page.select(HTML_SELECTOR_CHAPTER_NAVIGATION);
        final SiblingReference nextChapter = links.stream()
                .filter(element -> element.text().contains("Next") && element.text().contains("Chapter"))
                .filter(element -> !element.hasAttr("disabled"))
                .findAny()
                .map(element -> new SiblingReference(getIdentifier(element), bookIdentifier))
                .orElse(null);
        final SiblingReference previousChapter = links.stream()
                .filter(element -> element.text().contains("Previous") && element.text().contains("Chapter"))
                .filter(element -> !element.hasAttr("disabled"))
                .findAny()
                .map(element -> new SiblingReference(getIdentifier(element), bookIdentifier))
                .orElse(null);
        final Elements chapterContent = page.select(HTML_SELECTOR_CHAPTER_CONTENT);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter");
        checkProxyResult(chapterContent.isEmpty(), "Could not find chapter");
        return new Chapter(
                chapterIdentifier,
                bookIdentifier,
                chapterName,
                bookName,
                chapterContent.get(0).html(),
                nextChapter,
                previousChapter
        );
    }

    @Override
    public Book getBook(String bookIdentifier) throws ProxyException {
        final Document page = retrieveDocument(String.format(URL_PATTERN_BOOK, bookIdentifier));
        final String name = page.select(HTML_SELECTOR_BOOK_TITLE).stream().findAny().map(Element::text).orElse(bookIdentifier);
        final List<Reference> chapters = page.select(HTML_SELECTOR_BOOK_CHAPTERS).stream()
                .map(this::getChapterReference)
                .collect(Collectors.toList());
        return new Book(
                bookIdentifier,
                name,
                chapters
        );
    }

    @Override
    public List<Reference> getBooks() throws ProxyException {
        // When no favorites profile is configured use best rated
        if (StringUtils.isBlank(favoritesUserIdentifier)) {
            final Document page = retrieveDocument(URL_BEST_RATED);
            return page.select(HTML_SELECTOR_BEST_RATED_LINK).stream()
                    .map(this::getBookReference)
                    .collect(Collectors.toList());
        }
        final List<Reference> books = new LinkedList<>();
        // Collect books only from first 10 pages of favorites to keep loading times down
        for (int i = 1; i <= 10; i++) {
            final Document page = retrieveDocument(String.format(URL_PATTERN_FAVORITES, favoritesUserIdentifier, i));
            final Elements favorites = page.select(HTML_SELECTOR_FAVORITE);
            if (favorites.isEmpty()) {
                // Cancel further collection when current page has no more favorites
                continue;
            }
            for (final Element favorite : favorites) {
                final String title = favorite.select(HTML_SELECTOR_FAVORITE_BOOK_TITLE).stream().findAny().map(element -> element.attr("alt")).orElse(null);
                final String identifier = favorite.select(HTML_SELECTOR_FAVORITE_BOOK_LINK).stream().map(this::getIdentifier).findAny().orElse(null);
                if (identifier == null) {
                    continue;
                }
                books.add(new Reference(identifier, String.format(URL_PATTERN_BOOK, identifier), Objects.requireNonNullElse(title, identifier)));
            }
        }
        return books;
    }

    private Reference getBookReference(final Element link) {
        return new Reference(
                getIdentifier(link),
                link.attr("href"),
                link.text().strip()
        );
    }

    private Reference getChapterReference(final Element link) {
        return new Reference(
                getIdentifier(link),
                link.attr("href"),
                link.text().strip()
        );
    }

    private String getIdentifier(final Element link) {
        if (link == null) {
            log.warn("No Chapter link found");
            return "";
        }
        if ("disabled".equalsIgnoreCase(link.attr("disabled"))) {
            log.trace("Chapter does not exist or is disabled: {}", link);
            return "";
        }
        final String url = link.attr("href");
        final String[] urlParts = url.split("/");
        if (urlParts.length < 2) {
            log.warn("Found an invalid identifier link: {} in element: {}", url, link);
            return "";
        }
        return urlParts[urlParts.length - 2] + "/" + urlParts[urlParts.length - 1];
    }

}
