package de.bahmut.kindleproxy.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.bahmut.kindleproxy.handler.cleaner.ContentCleaner;
import de.bahmut.kindleproxy.handler.element.ElementCalculator;
import de.bahmut.kindleproxy.handler.element.ParagraphElementCalculator;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.Page;
import de.bahmut.kindleproxy.model.RenderedChapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.constant.CalculationConstants.BODY_PADDING;
import static de.bahmut.kindleproxy.constant.CalculationConstants.FONT_SIZE;

@Log4j2
@Service
@RequiredArgsConstructor
public class PageRenderService {

    private final UserSettingsService settingsService;

    private final CacheService cacheService;
    private final List<ContentCleaner> contentCleaners;
    private final List<ElementCalculator> elementCalculators;
    private final ParagraphElementCalculator paragraphElementCalculator;

    public RenderedChapter renderChapter(final Chapter chapter, final DeviceCalibration calibration) {
        final String cacheIdentifier = String.join(";", chapter.identifier(), chapter.bookIdentifier());
        final Optional<RenderedChapter> cachedChapter = cacheService.getCachedItem(cacheIdentifier, settingsService.getUserIdentifier().toString(), RenderedChapter.class);
        if (cachedChapter.isPresent()) {
            return cachedChapter.get();
        }
        final Document page = cleanPage(Jsoup.parseBodyFragment(chapter.htmlBody()));
        final Elements contentElements = page.select("body > *");
        final Map<Integer, Page> pages = renderPages(contentElements, calibration);
        final var render = new RenderedChapter(
                chapter.identifier(),
                chapter.bookIdentifier(),
                chapter.title(),
                pages,
                pages.keySet().stream().mapToInt(v -> v).max().orElse(1)
        );
        cacheService.addItemToCache(cacheIdentifier, settingsService.getUserIdentifier().toString(), render, Duration.ofDays(1));
        return render;
    }

    private Document cleanPage(final Document page) {
        Document cleanPage = page;
        for (final ContentCleaner cleaner : contentCleaners) {
            cleanPage = cleaner.clean(cleanPage);
        }
        return cleanPage;
    }

    private Map<Integer, Page> renderPages(final Elements contentElements, final DeviceCalibration calibration) {
        final Map<Integer, Page> pages = new HashMap<>();
        final var pageBuilder = new StringBuilder();
        int currentPage = 1;
        int currentPageHeight = BODY_PADDING;
        for (final Element element : contentElements) {
            final int elementHeight = calculateElementHeight(element, calibration);
            if (
                    exceedsPageSize(elementHeight, currentPageHeight, calibration.height()) &&
                    !isFirstElement(currentPage, currentPageHeight)
            ) {
                pages.put(currentPage, new Page(currentPage, pageBuilder.toString()));
                pageBuilder.setLength(0);
                currentPage++;
                currentPageHeight = BODY_PADDING;
            }
            pageBuilder.append(element).append("\n");
            currentPageHeight += elementHeight;
        }
        if (!pageBuilder.isEmpty()) {
            pages.put(currentPage, new Page(currentPage, pageBuilder.toString()));
        }
        return pages;
    }

    private boolean isFirstElement(final int pageNumber, final int pageHeight) {
        return pageNumber == 1 && pageHeight == BODY_PADDING;
    }

    private boolean exceedsPageSize(final int elementHeight, final int pageHeight, final int maxPageHeight) {
        return (pageHeight + elementHeight - FONT_SIZE) > maxPageHeight;
    }

    private int calculateElementHeight(final Element element, final DeviceCalibration calibration) {
        final Optional<ElementCalculator> elementCalculator = elementCalculators.stream()
                .filter(calculator -> calculator.isTagSupported(element.tag().normalName()))
                .findFirst();
        if (elementCalculator.isEmpty()) {
            log.warn("Could not find calculator for element: " + element.tag().normalName() + "; Using fallback calculator");
            return paragraphElementCalculator.calculateElementHeight(element, calibration);
        }
        return elementCalculator.get().calculateElementHeight(element, calibration);
    }

}
