package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

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
    public int calculateElementHeight(
            final Element element,
            final DeviceCalibration calibration,
            final UserSettings settings
    ) {
        final String[] textLines = element.html()
                .replace("</br>", "<br> ")
                .replace("<br>", "<br> ")
                .split("<br>");
        int elementHeight = 0;
        if (element.toString().startsWith("<p")) {
            // Paragraph padding
            elementHeight += settings.textSize();
        }
        for (String line : textLines) {
            final Document htmlLine = Jsoup.parse(line);
            elementHeight += htmlLine.getElementsByTag("img").stream()
                    .map(imageElement -> imageElementCalculator.calculateElementHeight(imageElement, calibration, settings))
                    .reduce(0, Integer::sum);
            elementHeight += calculateTextLineHeight(htmlLine, calibration, settings);
        }
        return elementHeight;
    }

}
