package de.bahmut.kindleproxy.web;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.BookReference;
import de.bahmut.kindleproxy.model.ChapterReference;
import de.bahmut.kindleproxy.service.ProxyService;
import de.bahmut.kindleproxy.util.StreamHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.web.util.UriUtils.encode;

@Controller
@RequiredArgsConstructor
public class BrowseController extends AbstractController {

    private final List<ProxyService> proxies;

    @GetMapping("/")
    public String home(
            final Model model
    ) {
        model.addAttribute("title", "Proxies");
        model.addAttribute("list", proxies.stream().collect(Collectors.toMap(ProxyService::getName, reference -> getProxyUrl(reference.getId()))));
        return "browse";
    }

    @GetMapping("/browse/proxy/{proxyId}/")
    public String browseProxy(
            @PathVariable("proxyId") final UUID proxyId,
            @RequestParam(value = "book", required = false) final String bookId,
            final Model model
    ) throws ProxyException {
        final Optional<ProxyService> proxy = findProxyService(proxyId, proxies);
        if (proxy.isEmpty()) {
            model.addAttribute("reason", "Proxy with id " + proxyId + " could not be found");
            return "error/404";
        }
        if (bookId != null) {
            final Book book = proxy.get().getBook(bookId);
            model.addAttribute("title", book.getName());
            model.addAttribute("list", book.getChapters().stream()
                    .collect(StreamHelper.toOrderedMap(ChapterReference::getName, reference -> RenderController.getRenderUrl(proxyId, bookId, reference.getIdentifier(), 1))));
            return "browse";
        }
        model.addAttribute("title", "Books");
        model.addAttribute("list", proxy.get().getBooks().stream()
                .collect(Collectors.toMap(BookReference::getName, reference -> getBookUrl(proxyId, reference.getIdentifier()))));
        return "browse";
    }

    public static String getProxyUrl(final UUID proxyId) {
        return "/browse/proxy/" + proxyId + "/";
    }

    public static String getBookUrl(final UUID proxyId, final String bookId) {
        return "/browse/proxy/" + proxyId + "/?book=" + encode(bookId, UTF_8);
    }

}
