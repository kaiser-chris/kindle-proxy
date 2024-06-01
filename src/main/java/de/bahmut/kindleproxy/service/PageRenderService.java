package de.bahmut.kindleproxy.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.bahmut.kindleproxy.handler.cleaner.ContentCleaner;
import de.bahmut.kindleproxy.handler.element.ElementCalculator;
import de.bahmut.kindleproxy.handler.element.ParagraphElementCalculator;
import de.bahmut.kindleproxy.handler.special.SpecialCaseHandler;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static de.bahmut.kindleproxy.util.PageSpacingCalculator.calculateBodyPadding;

@Log4j2
@Service
@RequiredArgsConstructor
public class PageRenderService {

    private final UserSettingsService settingsService;

    private final CacheService cacheService;
    private final List<ContentCleaner> contentCleaners;
    private final List<ElementCalculator> elementCalculators;
    private final List<SpecialCaseHandler> specialCaseHandlers;
    private final ParagraphElementCalculator paragraphElementCalculator;

    @Value("${settings.render.debug}")
    private boolean debugRender;

    public RenderedChapter renderChapter(final Chapter chapter, final DeviceCalibration calibration) {
        final String cacheIdentifier = String.join(";", chapter.identifier(), chapter.bookIdentifier());
        final Optional<RenderedChapter> cachedChapter = cacheService.getCachedItem(cacheIdentifier, settingsService.getUserIdentifier().toString(), RenderedChapter.class);
        if (cachedChapter.isPresent()) {
            return cachedChapter.get();
        }
        final Document page = cleanPage(Jsoup.parseBodyFragment(chapter.htmlBody()));
        final Elements contentElements = handleSpecialCases(page.select("body > *"));
        final Map<Integer, Page> pages = renderPages(contentElements, calibration);
        final var render = new RenderedChapter(
                chapter.identifier(),
                chapter.bookIdentifier(),
                chapter.chapterTitle(),
                chapter.bookTitle(),
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

    private Elements handleSpecialCases(final Elements contentElements) {
        final Elements workingElements = new Elements(contentElements);
        for (final SpecialCaseHandler handler : specialCaseHandlers) {
            for (final Element element : workingElements) {
                if (handler.isTagSupported(element.tagName())) {
                    var currentIndex = contentElements.indexOf(element);
                    contentElements.remove(element);
                    contentElements.addAll(currentIndex, handler.handleSpecialCase(element));
                }
            }
        }
        return contentElements;
    }

    private Map<Integer, Page> renderPages(final Elements contentElements, final DeviceCalibration calibration) {
        final Map<Integer, Page> pages = new HashMap<>();
        final var pageBuilder = new StringBuilder();
        int currentPage = 1;
        int currentPageHeight = calculateBodyPadding(settingsService.getSettings().textSize());
        for (final Element element : contentElements) {
            final int elementHeight = calculateElementHeight(element, calibration);
            if (
                    exceedsPageSize(elementHeight, currentPageHeight, calibration.height()) &&
                    !isFirstElement(currentPage, currentPageHeight)
            ) {
                pages.put(currentPage, new Page(currentPage, pageBuilder.toString()));
                pageBuilder.setLength(0);
                currentPage++;
                currentPageHeight = calculateBodyPadding(settingsService.getSettings().textSize());
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
        return pageNumber == 1 && pageHeight == calculateBodyPadding(settingsService.getSettings().textSize());
    }

    private boolean exceedsPageSize(final int elementHeight, final int pageHeight, final int maxPageHeight) {
        var textSize = settingsService.getSettings().textSize();
        if (settingsService.getSettings().footer()) {
            return (pageHeight + elementHeight - textSize) > (maxPageHeight - textSize);
        }
        return (pageHeight + elementHeight - textSize) > maxPageHeight;
    }

    private void addDebugInformation(final Element element, final int height) {
        final var debugInformation = new Element("span");
        debugInformation.attributes().add("class", "debug");
        debugInformation.text(height + "px");
        element.appendChild(debugInformation);
    }

    private int calculateElementHeight(final Element element, final DeviceCalibration calibration) {
        final int size;
        final Optional<ElementCalculator> elementCalculator = elementCalculators.stream()
                .filter(calculator -> calculator.isTagSupported(element.tag().normalName()))
                .findFirst();
        if (elementCalculator.isEmpty()) {
            log.warn("Could not find calculator for element: {}; Using fallback calculator", element.tag().normalName());
            size = paragraphElementCalculator.calculateElementHeight(element, calibration, settingsService.getSettings());
        } else {
            size = elementCalculator.get().calculateElementHeight(element, calibration, settingsService.getSettings());
        }
        if (debugRender) {
            addDebugInformation(element, size);
        }
        return size;
    }

}
