package de.bahmut.kindleproxy.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.Page;
import de.bahmut.kindleproxy.model.RenderedChapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class PageRenderService {

    private static final int FONT_SIZE = 24;

    private final CacheService cacheService;

    public RenderedChapter renderChapter(final Chapter chapter, final DeviceCalibration calibration) {
        return renderPages(chapter, calibration);
    }

    private RenderedChapter renderPages(final Chapter chapter, final DeviceCalibration calibration) {
        final String cacheIdentifier = String.join(";", chapter.identifier(), chapter.bookIdentifier(), calibration.getIdentifier().toString());
        final Optional<RenderedChapter> cachedChapter = cacheService.getCachedItem(cacheIdentifier, RenderedChapter.class);
        if (cachedChapter.isPresent()) {
            return cachedChapter.get();
        }
        final Document page = Jsoup.parseBodyFragment(chapter.htmlBody());
        final Elements contentElements = page.select("body > *");
        final Map<Integer, Page> pages = new HashMap<>();
        final var pageBuilder = new StringBuilder();
        int currentPage = 1;
        int currentPageHeight = calculateBodyPadding();
        for (final Element element : contentElements) {
            final int elementHeight = calculateElementHeight(element, calibration);
            // Element exceeds page height and is not the first element
            if ((currentPageHeight + elementHeight - FONT_SIZE) > calibration.height() && !(currentPage == 1 && currentPageHeight == calculateBodyPadding())) {
                pages.put(currentPage, new Page(currentPage, pageBuilder.toString()));
                pageBuilder.setLength(0);
                currentPage++;
                currentPageHeight = calculateBodyPadding();
            }
            pageBuilder
                    .append(element)
                    .append("\n");
            currentPageHeight += elementHeight;
        }
        final var render = new RenderedChapter(
                chapter.identifier(),
                chapter.bookIdentifier(),
                chapter.title(),
                pages,
                pages.keySet().stream().mapToInt(v -> v).max().orElse(1)
        );
        cacheService.addItemToCache(cacheIdentifier, render, Duration.ofDays(1));
        return render;
    }

    private int calculateBodyPadding() {
        // 2em on each side
        return 2 * 2 * FONT_SIZE;
    }

    private int calculateElementHeight(final Element element, final DeviceCalibration calibration) {
        return switch (element.tag().normalName()) {
            case "hr" -> FONT_SIZE + 1;
            case "table" -> calculateTableHeight(element, calibration);
            default -> calculateParagraphHeight(element, calibration);
        };
    }

    private int calculateTableHeight(final Element table, final DeviceCalibration calibration) {
        //TODO: Implement this
        return calculateParagraphHeight(table, calibration) + FONT_SIZE;
    }

    private int calculateParagraphHeight(final Element paragraph, final DeviceCalibration calibration) {
        final double lineHeight = Math.floor(FONT_SIZE * 1.4);
        final String[] textLines = paragraph.html()
                .replace("</br>", " <br>")
                .replace("<br>", " <br>")
                .split("<br>");
        int elementHeight = 0;
        if (paragraph.toString().startsWith("<p")) {
            // Paragraph padding
            elementHeight += FONT_SIZE;
        }
        for (String line : textLines) {
            final Document htmlLine = Jsoup.parse(line);
            elementHeight += calculateImageHeight(htmlLine);
            elementHeight += calculateTextLineHeight(htmlLine, lineHeight, calibration);
        }
        return elementHeight;
    }

    @SuppressWarnings("SameParameterValue")
    private int calculateTextLineHeight(final Document htmlLine, final double lineHeight, final DeviceCalibration calibration) {
        final String cleanLine = htmlLine.text();
        if (cleanLine.isEmpty() && !htmlLine.toString().isEmpty() && htmlLine.getElementsByTag("img").isEmpty()) {
            return (int) Math.ceil(lineHeight);
        }
        int currentLine = 1;
        double currentLineWidth = calculateBodyPadding();
        double splitWidth = calibration.calculateCharacterWidth(' ', FONT_SIZE);
        for (final String word : cleanLine.split(" ")) {
            double wordWidth = calculateWordWidth(word, calibration);
            wordWidth = wordWidth * 0.85; //TODO: Hack fix
            if ((currentLineWidth - (2 * splitWidth) + wordWidth) > calibration.width()) {
                currentLine++;
                currentLineWidth = calculateBodyPadding();
            }
            currentLineWidth += wordWidth;
        }
        log.debug(currentLine + " Lines in: " + cleanLine);
        return (int) Math.ceil(currentLine * lineHeight);
    }

    private double calculateWordWidth(final String word, final DeviceCalibration calibration) {
        double wordWidth = calibration.calculateCharacterWidth(' ', FONT_SIZE);
        for (char character : word.toCharArray()) {
            wordWidth += calibration.calculateCharacterWidth(character, FONT_SIZE);
        }
        return wordWidth;
    }

    private int calculateImageHeight(final Document htmlLine) {
        int height = 0;
        final Elements images = htmlLine.getElementsByTag("img");
        for (final Element imageElement : images) {
            final String heightValue = imageElement.attributes().get("height");
            if (StringUtils.isNotBlank(heightValue)) {
                height += Integer.parseInt(heightValue);
            } else {
                final String urlValue = imageElement.attributes().get("src");
                final String widthValue = imageElement.attributes().get("width");
                if (StringUtils.isBlank(urlValue)) {
                    continue;
                }
                final URL url;
                try {
                    url = new URL(urlValue);
                } catch (final MalformedURLException e) {
                    log.warn("Could not download embedded image because of an invalid url", e);
                    continue;
                }
                try (final InputStream stream = url.openStream()) {
                    BufferedImage image = ImageIO.read(stream);
                    if (StringUtils.isNotBlank(widthValue)) {
                        final double ratio = Double.parseDouble(widthValue) / (double) image.getWidth();
                        height += image.getHeight() * ratio;
                    } else {
                        height += image.getHeight();
                    }
                } catch (final IOException e) {
                    log.warn("Could not download embedded image", e);
                    height += FONT_SIZE;
                }
            }
        }
        return height;
    }

}
