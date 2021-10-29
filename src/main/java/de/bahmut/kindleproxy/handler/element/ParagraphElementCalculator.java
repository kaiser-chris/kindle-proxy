package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import static de.bahmut.kindleproxy.constant.CalculationConstants.FONT_SIZE;

@Log4j2
@Component
@RequiredArgsConstructor
public class ParagraphElementCalculator extends AbstractLineCalculator {

    private final ImageElementCalculator imageElementCalculator;

    @Override
    public boolean isTagSupported(final String tag) {
        return "p".equalsIgnoreCase(tag)
                || "div".equalsIgnoreCase(tag)
                || "span".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(Element element, DeviceCalibration calibration) {
        final String[] textLines = element.html()
                .replace("</br>", "<br> ")
                .replace("<br>", "<br> ")
                .split("<br>");
        int elementHeight = 0;
        if (element.toString().startsWith("<p")) {
            // Paragraph padding
            elementHeight += FONT_SIZE;
        }
        for (String line : textLines) {
            final Document htmlLine = Jsoup.parse(line);
            elementHeight += htmlLine.getElementsByTag("img").stream()
                    .map(imageElement -> imageElementCalculator.calculateElementHeight(imageElement, calibration))
                    .reduce(0, Integer::sum);
            elementHeight += calculateTextLineHeight(htmlLine, calibration);
        }
        return elementHeight;
    }

}
