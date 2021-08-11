package de.bahmut.kindleproxy.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import de.bahmut.kindleproxy.exception.NotFoundException;
import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Book;
import de.bahmut.kindleproxy.model.Reference;
import de.bahmut.kindleproxy.service.proxy.ProxyService;
import de.bahmut.kindleproxy.util.StreamHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.web.util.UriUtils.encode;

@Controller
@RequiredArgsConstructor
public class BrowseController extends AbstractController {

    private final List<ProxyService> proxies;

    @GetMapping("/")
    public ModelAndView home() {
        final var webPage = createBrowseModelAndView();
        webPage.addObject("title", "Proxies");
        webPage.addObject("list", proxies.stream().collect(Collectors.toMap(ProxyService::getName, reference -> getProxyUrl(reference.getId()))));
        return webPage;
    }

    @GetMapping("/browse/proxy/{proxyId}/")
    public ModelAndView browseProxy(
            @PathVariable("proxyId") final UUID proxyId,
            @RequestParam(value = "book", required = false) final String bookId
    ) throws ProxyException {
        final Optional<ProxyService> proxy = findProxyService(proxyId, proxies);
        if (proxy.isEmpty()) {
            throw new NotFoundException("Proxy with id " + proxyId + " could not be found");
        }
        final var webPage = createBrowseModelAndView();
        if (bookId != null) {
            final Book book = proxy.get().getBook(bookId);
            webPage.addObject("title", book.name());
            webPage.addObject("list", book.chapters().stream().collect(StreamHelper.toOrderedMap(
                            Reference::name,
                            reference -> RenderController.getRenderUrl(proxyId, bookId, reference.identifier(), 1)
            )));
            return webPage;
        }
        webPage.addObject("title", "Books");
        webPage.addObject("list", proxy.get().getBooks().stream().collect(StreamHelper.toOrderedMap(
                Reference::name,
                reference -> getBookUrl(proxyId, reference.identifier())
        )));
        return webPage;
    }

    private ModelAndView createBrowseModelAndView() {
        final var modelAndView = new ModelAndView();
        modelAndView.setViewName("browse");
        return modelAndView;
    }

    public static String getProxyUrl(final UUID proxyId) {
        return "/browse/proxy/" + proxyId + "/";
    }

    public static String getBookUrl(final UUID proxyId, final String bookId) {
        return "/browse/proxy/" + proxyId + "/?book=" + encode(bookId, UTF_8);
    }

}
