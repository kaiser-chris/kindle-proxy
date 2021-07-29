package de.bahmut.kindleproxy.service.proxy;

import java.io.IOException;
import java.util.Optional;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

@RequiredArgsConstructor
public abstract class CachedWebProxyService implements ProxyService {

    private final CacheService cacheService;

    protected Document retrieveDocument(final String url) throws ProxyException {
        final Optional<Document> cachedPage = cacheService.getCachedItem(url, Document.class);
        if (cachedPage.isPresent()) {
            return cachedPage.get();
        }
        final Document page;
        try {
            page = Jsoup.connect(url).get();
        } catch (final IOException e) {
            throw new ProxyException("Could not retrieve html: " + url, e);
        }
        cacheService.addItemToCache(url, page);
        return page;
    }


}
