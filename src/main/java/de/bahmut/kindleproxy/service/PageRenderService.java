package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.constant.Device;
import de.bahmut.kindleproxy.model.Content;
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

    private final TemplateCacheService templateService;

    public String renderPage(final Content content, final int pageNumber, final Device device) {
        final String pageTemplate = templateService.getTemplate("page");
        final Content pageContent = calculatePage(content, pageNumber, device);
        return String.format(
                pageTemplate,
                device.getCssFile(),
                pageContent.getTitle(),
                pageNumber - 1,
                pageNumber + 1,
                pageContent.getBody()
        );
    }

    private Content calculatePage(final Content content, final int pageNumber, final Device device) {
        if (device.getWidth() < 0 || device.getHeight() < 0) {
            return content;
        }
        final Document page = Jsoup.parse(content.getBody());
        final Elements paragraphs = page.getElementsByTag("p");
        final var pageBuilder = new StringBuilder();
        int currentPage = 1;
        int currentPageHeight = 0;
        for (final Element paragraph : paragraphs) {
            if (currentPage > pageNumber) {
                break;
            }
            final int elementHeight = calculateParagraphHeight(paragraph, device);
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
        return new Content(content.getTitle(), pageBuilder.toString());
    }

    private int calculateParagraphHeight(final Element paragraph, final Device device) {
        final double lineHeight = device.getFontSize() * 1.4;
        final String[] textLines = paragraph.html().split("<br>|<br/>");
        int elementHeight = device.getFontSize(); // Padding
        for (String line : textLines) {
            final Document htmlLine = Jsoup.parse(line);
            elementHeight += calculateImageHeight(htmlLine);
            elementHeight += calculateTextLineHeight(htmlLine, lineHeight);
        }
        return elementHeight;
    }

    private int calculateTextLineHeight(final Document htmlLine, final double lineHeight) {
        final String cleanLine = htmlLine.text();
        if (cleanLine.isEmpty() && !htmlLine.toString().isEmpty() && htmlLine.getElementsByTag("img").isEmpty()) {
            return (int) Math.ceil(lineHeight);
        } else {
            return (int) Math.ceil(Math.ceil(((double) cleanLine.length() / 52)) * lineHeight);
        }
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
