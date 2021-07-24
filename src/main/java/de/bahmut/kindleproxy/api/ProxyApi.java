package de.bahmut.kindleproxy.api;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import de.bahmut.kindleproxy.constant.Device;
import de.bahmut.kindleproxy.exception.ProxyException;
import de.bahmut.kindleproxy.model.Content;
import de.bahmut.kindleproxy.service.PageRenderService;
import de.bahmut.kindleproxy.service.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequiredArgsConstructor
public class ProxyApi {

    private final ProxyService proxyService;
    private final PageRenderService renderService;

    @GetMapping("/")
    public ResponseEntity<String> get(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestHeader("User-Agent") String agent
    ) throws ProxyException {
        final Device device;
        if (agent.contains("Kindle")) {
            device = Device.KINDLE_PAPERWHITE;
        } else {
            device = Device.KINDLE_PAPERWHITE;
        }
        final Content content = proxyService.getChapter(null, null);
        final String pageContent = renderService.renderPage(content, page, device);
        return ResponseEntity.ok(pageContent);
    }

}
