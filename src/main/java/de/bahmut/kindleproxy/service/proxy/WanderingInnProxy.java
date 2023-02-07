package de.bahmut.kindleproxy.service.proxy;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.*;
import de.bahmut.kindleproxy.service.CacheService;
import de.bahmut.kindleproxy.util.ListHelper;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.bahmut.kindleproxy.util.check.ProxyConditions.checkProxyResult;

@Log4j2
@Service
public class WanderingInnProxy extends CachedWebProxyService {
    private static final String BASE_URL = "https://wanderinginn.com/";
    private static final String URL_TABLE_OF_CONTENTS = BASE_URL + "table-of-contents/";
    private static final String HTML_SELECTOR_CHAPTERS_AND_VOLUMES = "div#content div.entry-content p a,strong";
    private static final String HTML_SELECTOR_CHAPTER_CONTENT = "div#content div.entry-content";

    public WanderingInnProxy(CacheService cacheService) {
        super(cacheService);
    }

    @Override
    public String getName() {
        return "The Wandering Inn";
    }

    @Override
    public Chapter getChapter(String bookIdentifier, String chapterIdentifier) throws ProxyException {
        final Proxy cachedProxy = getOrCreateProxy();
        final Optional<Book> foundBook = cachedProxy.books().stream()
                .filter(book -> book.identifier().equalsIgnoreCase(bookIdentifier))
                .findFirst();
        checkProxyResult(foundBook.isEmpty(), "Could not find volume");
        final Optional<Reference> foundChapter = foundBook.get().chapters().stream()
                .filter(chapter -> chapter.identifier().equalsIgnoreCase(chapterIdentifier))
                .findFirst();
        checkProxyResult(foundChapter.isEmpty(), "Could not find chapter");
        final Document page = retrieveDocument(foundChapter.get().link());
        final Elements chapterContent = page.select(HTML_SELECTOR_CHAPTER_CONTENT);
        checkProxyResult(chapterContent.size() > 1, "Found more than one chapter");
        checkProxyResult(chapterContent.size() == 0, "Could not find chapter");
        // Clean up pasted chapters
        if (chapterContent.get(0).children().size() > 0 && "paste-embed-wrapper".equalsIgnoreCase(chapterContent.get(0).children().get(0).id())) {
            final Element contentElement = chapterContent.get(0);
            final Element pasteWrapper = chapterContent.get(0).children().get(0);
            final Elements otherChildren = contentElement.children().clone();
            otherChildren.remove(0);
            contentElement.children().remove();
            for (Element wrapperContentElement : pasteWrapper.children()) {
                wrapperContentElement.appendTo(contentElement);
            }
            for (Element existingElement : otherChildren) {
                existingElement.appendTo(contentElement);
            }
            log.debug("Fixed paste wrapper");
        }
        return new Chapter(
                chapterIdentifier,
                bookIdentifier,
                foundChapter.get().name(),
                getName(),
                chapterContent.get(0).html(),
                getNextChapter(foundChapter.get(), foundBook.get(), cachedProxy),
                getPreviousChapter(foundChapter.get(), foundBook.get(), cachedProxy)
        );
    }

    private SiblingReference getPreviousChapter(final Reference chapter, final Book volume, final Proxy proxy) {
        final Optional<Reference> previousInVolume = ListHelper.getPrevious(chapter, volume.chapters());
        if (previousInVolume.isPresent()) {
            return new SiblingReference(previousInVolume.get().identifier(), volume.identifier());
        }
        final Optional<Book> previousVolume = ListHelper.getPrevious(volume, proxy.books());
        if (previousVolume.isEmpty()) {
            return null;
        }
        return previousVolume
                .map(book -> book.chapters().get(book.chapters().size() - 1).identifier())
                .map(siblingChapterIdentifier -> new SiblingReference(siblingChapterIdentifier, previousVolume.get().identifier()))
                .orElse(null);
    }

    private SiblingReference getNextChapter(final Reference chapter, final Book volume, final Proxy proxy) {
        final Optional<Reference> nextInVolume = ListHelper.getNext(chapter, volume.chapters());
        if (nextInVolume.isPresent()) {
            return new SiblingReference(nextInVolume.get().identifier(), volume.identifier());
        }
        final Optional<Book> nextVolume = ListHelper.getNext(volume, proxy.books());
        if (nextVolume.isEmpty()) {
            return null;
        }
        return nextVolume
                .map(book -> book.chapters().get(0).identifier())
                .map(siblingChapterIdentifier -> new SiblingReference(siblingChapterIdentifier, nextVolume.get().identifier()))
                .orElse(null);
    }

    @Override
    public Book getBook(String bookIdentifier) throws ProxyException {
        final Proxy cachedProxy = getOrCreateProxy();
        final Optional<Book> foundBook = cachedProxy.books().stream()
                .filter(book -> book.identifier().equalsIgnoreCase(bookIdentifier))
                .findFirst();
        checkProxyResult(foundBook.isEmpty(), "Could not find volume");
        return foundBook.get();
    }

    @Override
    public List<Reference> getBooks() throws ProxyException {
        final Proxy cachedProxy = getOrCreateProxy();
        return cachedProxy.books().stream()
                .map(book -> new Reference(book.identifier(), book.identifier(), book.name()))
                .collect(Collectors.toList());
    }

    private Proxy getOrCreateProxy() throws ProxyException {
        final Optional<Proxy> cachedProxy = cacheService.getCachedItem(getName(), Proxy.class);
        if (cachedProxy.isPresent()) {
            return cachedProxy.get();
        }
        final Proxy proxy = new Proxy(
                "inn",
                getName(),
                new LinkedList<>()
        );
        final Document page = retrieveDocument(URL_TABLE_OF_CONTENTS);
        final List<Element> entries = page.select(HTML_SELECTOR_CHAPTERS_AND_VOLUMES);
        Book currentVolume = null;
        for (final Element entry : entries) {
            // Volume Titles are strong-Elements
            if (entry.tag().getName().equalsIgnoreCase("strong")) {
                final String title = entry.text().strip();
                currentVolume = new Book(
                        UUID.nameUUIDFromBytes(title.getBytes()).toString(),
                        title,
                        new LinkedList<>()
                );
                proxy.books().add(currentVolume);
            }
            // Chapters are a-Elements
            if (entry.tag().getName().equalsIgnoreCase("a")) {
                checkProxyResult(currentVolume == null, "Found chapter in table of contents before first volume");
                final String title = entry.text().strip();
                final String link = entry.attr("href");
                currentVolume.chapters().add(new Reference(
                        getChapterIdentifier(link),
                        link,
                        title
                ));
            }
        }
        cacheService.addItemToCache(getName(), proxy);
        return proxy;
    }

    private String getChapterIdentifier(final String link) {
        return UUID.nameUUIDFromBytes(link.toLowerCase().getBytes()).toString();
    }

}
