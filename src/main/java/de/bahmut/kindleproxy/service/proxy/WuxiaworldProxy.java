package de.bahmut.kindleproxy.service.proxy;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.Reference;
import de.bahmut.kindleproxy.service.CacheService;
import lombok.extern.log4j.Log4j2;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Log4j2
@Service
public class WuxiaworldProxy extends CachedWebProxyService {

    private static final String BASE_URL = "https://www.wuxiaworld.com/";

    private static final String URL_API_BOOK_LIST = BASE_URL + "api/novels/search";
    private static final String API_BOOK_LIST_PAYLOAD = "{\"title\":\"\",\"tags\":[],\"language\":\"Any\",\"genres\":[],\"active\":null,\"sortType\":\"Name\",\"sortAsc\":true,\"searchAfter\":null,\"count\":1000}";

    private static final String URL_PATTERN_BOOK = BASE_URL + "novel/%s";
    private static final String HTML_SELECTOR_BOOK_TITLE = "div.novel-body h2";
    private static final String HTML_SELECTOR_BOOK_CHAPTERS = "div.novel-content li.chapter-item a";

    private static final String URL_PATTERN_CHAPTER = BASE_URL + "novel/%s/%s";
    private static final String HTML_SELECTOR_CHAPTER_TITLE = "div#chapter-outer h4";
    private static final String HTML_SELECTOR_CHAPTER_CONTENT = "div#chapter-content";
    private static final String HTML_SELECTOR_CHAPTER_NAVIGATION_NEXT = "div.top-bar-area li.next a.btn.btn-link:not(.disabled)";
    private static final String HTML_SELECTOR_CHAPTER_NAVIGATION_PREVIOUS = "div.top-bar-area li.prev a.btn.btn-link:not(.disabled)";

    private final RestTemplate restTemplate;

    public WuxiaworldProxy(
            final CacheService cacheService,
            final RestTemplate restTemplate
    ) {
        super(cacheService);
        this.restTemplate = restTemplate;
    }

    @Override
    public String getName() {
        return "Wuxiaworld";
    }

    @Override
    public Chapter getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final String chapterUrl = String.format(URL_PATTERN_CHAPTER, bookIdentifier, chapterIdentifier);
        final Document page = retrieveDocument(chapterUrl);
        final Elements linksPrevious = page.select(HTML_SELECTOR_CHAPTER_NAVIGATION_PREVIOUS);
        final Elements linksNext = page.select(HTML_SELECTOR_CHAPTER_NAVIGATION_NEXT);
        final Elements title = page.select(HTML_SELECTOR_CHAPTER_TITLE);
        checkProxyResult(title.size() > 1, "Found more than one chapter title");
        checkProxyResult(title.size() == 0, "Could not find chapter title");
        final Elements chapterContent = page.select(HTML_SELECTOR_CHAPTER_CONTENT);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter title");
        checkProxyResult(chapterContent.size() == 0, "Could not find chapter title");
        return new Chapter(
                chapterIdentifier,
                bookIdentifier,
                title.get(0).text().trim(),
                chapterContent.get(0).html(),
                extractIdentifier(linksNext),
                extractIdentifier(linksPrevious)
        );
    }

    @Override
    public Book getBook(String bookIdentifier) throws ProxyException {
        final Document page = retrieveDocument(String.format(URL_PATTERN_BOOK, bookIdentifier));
        final Elements title = page.select(HTML_SELECTOR_BOOK_TITLE);
        checkProxyResult(title.size() > 1, "Found more than one chapter title");
        checkProxyResult(title.size() == 0, "Could not find chapter title");
        final List<Reference> chapters = page.select(HTML_SELECTOR_BOOK_CHAPTERS).stream()
                .map(this::extractReference)
                .collect(Collectors.toList());
        return new Book(
                bookIdentifier,
                title.text(),
                chapters
        );
    }

    @Override
    public List<Reference> getBooks() throws ProxyException {
        final List<Reference> books = new LinkedList<>();
        final MultiValueMap<String, String> headers = new HttpHeaders();
        headers.add("accept", "application/json");
        headers.add("content-type", "application/json;charset=UTF-8");
        final URI apiUrl;
        try {
            apiUrl = new URI(URL_API_BOOK_LIST);
            final ResponseEntity<String> result = restTemplate.exchange(apiUrl, HttpMethod.POST, new HttpEntity<>(API_BOOK_LIST_PAYLOAD, headers), String.class);
            JSONObject jsonResult = new JSONObject(result.getBody());
            final JSONArray jsonBooks = jsonResult.getJSONArray("items");
            for (int i = 0; i < jsonBooks.length(); i++) {
                final JSONObject jsonBook = jsonBooks.getJSONObject(i);
                books.add(new Reference(
                        jsonBook.getString("slug"),
                        jsonBook.getString("name")
                ));
            }
        } catch (final URISyntaxException | JSONException e) {
            throw new ProxyException("Could not parse Wuxiaworld book list", e);
        }
        return books;
    }

    private String extractIdentifier(final Elements links) {
        if (links.size() == 0) {
            return null;
        }
        if (links.size() > 1) {
            log.warn("Found too many chapter links" + links);
        }
        final Reference chapterReference = extractReference(links.get(0));
        if (chapterReference == null) {
            return null;
        } else {
            return chapterReference.identifier();
        }
    }

    private Reference extractReference(final Element link) {
        final String url = link.attr("href");
        final String[] urlParts = url.split("/");
        if (urlParts.length < 1) {
            log.warn("Found an invalid identifier link: " + url + " in element: " + link);
            return null;
        }
        return new Reference(
                urlParts[urlParts.length - 1],
                link.text()
        );
    }

}
