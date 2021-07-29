package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

    public Chapter renderPage(final Chapter chapter, final int pageNumber, final DeviceCalibration calibration) {
        return calculatePage(chapter, pageNumber, calibration);
    }

    private Chapter calculatePage(final Chapter chapter, final int pageNumber, final DeviceCalibration calibration) {
        final Document page = Jsoup.parse(chapter.getBody());
        final Elements paragraphs = page.getElementsByTag("p");
        final var pageBuilder = new StringBuilder();
        int currentPage = 1;
        int currentPageHeight = calculateBodyPadding();
        for (final Element paragraph : paragraphs) {
            if (currentPage > pageNumber) {
                break;
            }
            final int elementHeight = calculateParagraphHeight(paragraph, calibration);
            if ((currentPageHeight + elementHeight - FONT_SIZE) > calibration.getHeight()) {
                currentPage++;
                currentPageHeight = calculateBodyPadding();
            }
            if (currentPage == pageNumber) {
                pageBuilder
                        .append(paragraph)
                        .append("\n");
            }
            currentPageHeight += elementHeight;
        }
        return new Chapter(
                chapter.getIdentifier(),
                chapter.getBookIdentifier(),
                chapter.getTitle(),
                pageBuilder.toString(),
                chapter.getNextChapterIdentifier(),
                chapter.getPreviousChapterIdentifier()
        );
    }

    private int calculateBodyPadding() {
        // 2em on each side
        return 2 * 2 * FONT_SIZE;
    }

    private int calculateParagraphHeight(final Element paragraph, final DeviceCalibration calibration) {
        final double lineHeight = Math.floor(FONT_SIZE * 1.4);
        final String[] textLines = paragraph.html().split("<br>|<br/>");
        int elementHeight = FONT_SIZE; // Paragraph padding
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
            if ((currentLineWidth - (2 * splitWidth) + wordWidth) > calibration.getWidth()) {
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
        for (final Element image : images) {
            height += Integer.parseInt(image.attributes().get("height"));
        }
        return height;
    }

}
