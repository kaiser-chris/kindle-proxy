package de.bahmut.kindleproxy.web;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.bahmut.kindleproxy.exception.NotFoundException;
import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.service.CalibrationCacheService;
import de.bahmut.kindleproxy.service.PageRenderService;
import de.bahmut.kindleproxy.service.proxy.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.web.util.UriUtils.encode;

@Log4j2
@Controller
@RequiredArgsConstructor
public class RenderController extends AbstractController {

    private final List<ProxyService> proxies;
    private final PageRenderService renderService;
    private final CalibrationCacheService calibrationCacheService;

    @GetMapping("/render/proxy/{proxyId}/")
    public ModelAndView render(
            @PathVariable("proxyId") final UUID proxyId,
            @RequestParam("book") final String bookId,
            @RequestParam("chapter") final String chapterId,
            @RequestParam(value = "page", required = false, defaultValue = "1") final int page,
            @RequestHeader("User-Agent") final String agent
    ) throws ProxyException, IOException {
        final Optional<ProxyService> proxy = findProxyService(proxyId, proxies);
        if (proxy.isEmpty()) {
            throw new NotFoundException("Proxy with id " + proxyId + " could not be found");
        }
        if (page <= 0) {
            return new ModelAndView("redirect:" + getRenderUrl(proxyId, bookId, chapterId, 1));
        }
        final Chapter chapter = proxy.get().getChapter(bookId, chapterId);
        final Optional<DeviceCalibration> calibration = calibrationCacheService.findCalibration(agent);
        if (calibration.isEmpty()) {
            return new ModelAndView("redirect:/calibrate/?redirect=" + encode(getRenderUrl(proxyId, bookId, chapterId, page), UTF_8));
        }
        final Chapter chapterPage = renderService.renderPage(chapter, page, calibration.get());
        final var webPage = new ModelAndView();
        webPage.setViewName("render");
        webPage.addObject("proxyId", proxyId);
        webPage.addObject("bookId", encode(bookId, UTF_8));
        webPage.addObject("chapterId", encode(chapterId, UTF_8));
        webPage.addObject("page", page);
        webPage.addObject("content", chapterPage);
        webPage.addObject("next", getRenderUrl(proxyId, bookId, chapterPage.getNextChapterIdentifier(), 1));
        webPage.addObject("book", BrowseController.getBookUrl(proxyId, bookId));
        webPage.addObject("previous", getRenderUrl(proxyId, bookId, chapterPage.getPreviousChapterIdentifier(), 1));
        return webPage;
    }

    public static String getRenderUrl(
            final UUID proxyId,
            final String bookId,
            final String chapterId,
            final Integer page
    ) {
        if (proxyId == null || bookId == null || chapterId == null) {
            return null;
        }
        final String baseUrl = "/render/proxy/" + proxyId + "/?book=" + encode(bookId, UTF_8) + "&chapter=" + encode(chapterId, UTF_8);
        if (page != null) {
            return baseUrl + "&page=" + page;
        } else {
            return baseUrl;
        }
    }

}
