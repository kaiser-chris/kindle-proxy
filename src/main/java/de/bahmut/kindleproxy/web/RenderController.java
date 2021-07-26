package de.bahmut.kindleproxy.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import de.bahmut.kindleproxy.constant.Device;
import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Content;
import de.bahmut.kindleproxy.model.font.DeviceCalibration;
import de.bahmut.kindleproxy.service.CalibrationCacheService;
import de.bahmut.kindleproxy.service.PageRenderService;
import de.bahmut.kindleproxy.service.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.web.util.UriUtils.encode;

@Log4j2
@Controller
@RequiredArgsConstructor
public class RenderController {

    private final ProxyService proxyService;
    private final PageRenderService renderService;
    private final CalibrationCacheService calibrationCacheService;

    @GetMapping("/render/proxy/{proxyId}/")
    public String render(
            @PathVariable("proxyId") final UUID proxyId,
            @RequestParam("book") final String bookId,
            @RequestParam("chapter") final String chapterId,
            @RequestParam(value = "page", required = false, defaultValue = "1") final int page,
            @RequestHeader("User-Agent") final String agent,
            final Model model
    ) throws ProxyException, IOException {
        if (page <= 0) {
            return "redirect:" + buildUrl(proxyId, bookId, chapterId, 1);
        }
        final Content content = proxyService.getChapter(bookId, chapterId);
        final Optional<DeviceCalibration> calibration = calibrationCacheService.findCalibration(agent);
        if (calibration.isEmpty()) {
            return "redirect:/calibrate/?redirect=" + encode(buildUrl(proxyId, bookId, chapterId, page), UTF_8);
        }
        final Content pageContent = renderService.renderPage(content, page, Device.KINDLE_PAPERWHITE, calibration.get());
        model.addAttribute("proxyId", proxyId);
        model.addAttribute("bookId", encode(bookId, UTF_8));
        model.addAttribute("chapterId", encode(chapterId, UTF_8));
        model.addAttribute("page", page);
        model.addAttribute("content", pageContent);
        model.addAttribute("device", calibration.get().getDevice());
        model.addAttribute("next", buildUrl(proxyId, bookId, pageContent.getNextContent(), 1));
        model.addAttribute("previous", buildUrl(proxyId, bookId, pageContent.getPreviousContent(), 1));
        return "render";
    }

    private String buildUrl(
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
