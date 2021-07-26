package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.constant.Device;
import de.bahmut.kindleproxy.model.Chapter;
import de.bahmut.kindleproxy.model.font.DeviceCalibration;
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

    public Chapter renderPage(final Chapter chapter, final int pageNumber, final Device device, final DeviceCalibration calibration) {
        return calculatePage(chapter, pageNumber, device, calibration);
    }

    private Chapter calculatePage(final Chapter chapter, final int pageNumber, final Device device, final DeviceCalibration calibration) {
        if (device.getWidth() < 0 || device.getHeight() < 0) {
            return chapter;
        }
        final Document page = Jsoup.parse(chapter.getBody());
        final Elements paragraphs = page.getElementsByTag("p");
        final var pageBuilder = new StringBuilder();
        int currentPage = 1;
        int currentPageHeight = 0;
        for (final Element paragraph : paragraphs) {
            if (currentPage > pageNumber) {
                break;
            }
            final int elementHeight = calculateParagraphHeight(paragraph, device, calibration);
            if ((currentPageHeight + elementHeight) > device.getHeight()) {
                currentPage++;
                currentPageHeight = 0;
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

    private int calculateParagraphHeight(final Element paragraph, final Device device, final DeviceCalibration calibration) {
        final double lineHeight = device.getFontSize() * 1.4;
        final String[] textLines = paragraph.html().split("<br>|<br/>");
        int elementHeight = device.getFontSize(); // Padding
        for (String line : textLines) {
            final Document htmlLine = Jsoup.parse(line);
            elementHeight += calculateImageHeight(htmlLine);
            elementHeight += calculateTextLineHeight(htmlLine, lineHeight, device, calibration);
        }
        return elementHeight;
    }

    private int calculateTextLineHeight(final Document htmlLine, final double lineHeight, final Device device, final DeviceCalibration calibration) {
        final String cleanLine = htmlLine.text();
        if (cleanLine.isEmpty() && !htmlLine.toString().isEmpty() && htmlLine.getElementsByTag("img").isEmpty()) {
            return (int) Math.ceil(lineHeight);
        }
        int currentLine = 1;
        double currentLineWidth = 0;
        double splitWidth = calibration.calculateCharacterWidth(' ', device.getFontSize());
        for (final String word : cleanLine.split(" ")) {
            double wordWidth = calculateWordWidth(word, device, calibration);
            wordWidth = wordWidth * 0.85; //TODO: Hack fix
            if ((currentLineWidth - (2 * splitWidth) + wordWidth) > device.getWidth()) {
                currentLine++;
                currentLineWidth = 0;
            }
            currentLineWidth += wordWidth;
        }
        log.debug(currentLine + " Lines in: " + cleanLine);
        return (int) Math.ceil(currentLine * lineHeight);
    }

    private double calculateWordWidth(final String word, final Device device, final DeviceCalibration calibration) {
        double wordWidth = calibration.calculateCharacterWidth(' ', device.getFontSize());
        for (char character : word.toCharArray()) {
            wordWidth += calibration.calculateCharacterWidth(character, device.getFontSize());
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
