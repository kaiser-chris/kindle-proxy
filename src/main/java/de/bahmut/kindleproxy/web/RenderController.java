package de.bahmut.kindleproxy.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.exception.NotFoundException;
import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.RenderedChapter;
import de.bahmut.kindleproxy.model.SiblingReference;
import de.bahmut.kindleproxy.service.PageRenderService;
import de.bahmut.kindleproxy.service.UserSettingsService;
import de.bahmut.kindleproxy.service.proxy.ProxyService;
import de.bahmut.kindleproxy.web.base.ProxyBasedController;
import de.bahmut.kindleproxy.web.base.RenderingController;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.web.util.UriUtils.encode;

@Log4j2
@Controller
@RequiredArgsConstructor
public class RenderController implements ProxyBasedController, RenderingController {

    private final UserSettingsService settingsService;
    private final List<ProxyService> proxies;
    private final PageRenderService renderService;

    @Value("${settings.render.debug}")
    private boolean debugRender;

    @GetMapping("/render/proxy/{proxyId}/")
    public ModelAndView render(
            @PathVariable("proxyId") final UUID proxyId,
            @RequestParam("book") final String bookId,
            @RequestParam("chapter") final String chapterId,
            @RequestParam(value = "page", required = false, defaultValue = "1") final int page,
            @RequestParam(value = "source", required = false) final String source
    ) throws ProxyException, CalibrationException {
        final Optional<ProxyService> proxy = findProxyService(proxyId, proxies);
        if (proxy.isEmpty()) {
            throw new NotFoundException("Proxy with id " + proxyId + " could not be found");
        }
        final Optional<DeviceCalibration> calibration = settingsService.getCalibration();
        if (calibration.isEmpty()) {
            return new ModelAndView("redirect:" + CalibrateController.getCalibrationUrl(getRenderUrl(proxyId, bookId, chapterId, page)));
        }
        final Chapter chapter = proxy.get().getChapter(bookId, chapterId);
        final RenderedChapter renderedChapter = renderService.renderChapter(chapter, calibration.get());
        final String previousChapter = getRenderUrl(
                proxyId,
                chapter.previousChapter(),
                1
        );
        final String nextChapter = getRenderUrl(
                proxyId,
                chapter.nextChapter(),
                1
        );
        final var webPage = new ModelAndView();
        if (page == 0 && previousChapter != null) {
            webPage.setViewName("render/previous-chapter");
        } else if (page == renderedChapter.maxPage() + 1 && nextChapter != null) {
            webPage.setViewName("render/next-chapter");
        } else if (page <= 0 && previousChapter == null) {
            return new ModelAndView("redirect:" + getRenderUrl(proxyId, bookId, chapterId, 1));
        } else if (page > renderedChapter.maxPage() && nextChapter == null) {
            return new ModelAndView("redirect:" + getRenderUrl(proxyId, bookId, chapterId, renderedChapter.maxPage()));
        } else if ("prev".equalsIgnoreCase(source)) {
            return new ModelAndView("redirect:" + getRenderUrl(proxyId, bookId, chapterId, renderedChapter.maxPage()));
        } else {
            webPage.setViewName("render/render");
        }
        webPage.addObject("proxyId", proxyId);
        webPage.addObject("bookId", encode(bookId, UTF_8));
        webPage.addObject("chapterId", encode(chapterId, UTF_8));
        webPage.addObject("page", page);
        webPage.addObject("chapter", renderedChapter);
        webPage.addObject("settings", settingsService.getSettings());
        webPage.addObject("settingsUrl", SettingsController.getSettingsUrl(getRenderUrl(proxyId, bookId, chapterId, page)));
        webPage.addObject("next", nextChapter);
        webPage.addObject("book", BrowseController.getBookUrl(proxyId, bookId));
        webPage.addObject("previous", previousChapter);
        webPage.addObject("debug", debugRender);
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

    public static String getRenderUrl(
            final UUID proxyId,
            final SiblingReference reference,
            final Integer page
    ) {
        if (proxyId == null || reference == null) {
            return null;
        }
        return getRenderUrl(proxyId, reference.siblingBookIdentifier(), reference.siblingChapterIdentifier(), page);
    }

}
