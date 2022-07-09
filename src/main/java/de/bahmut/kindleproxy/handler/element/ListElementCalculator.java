package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import static de.bahmut.kindleproxy.util.PageSpacingCalculator.calculateBodyPadding;

@Component
public class ListElementCalculator extends AbstractLineCalculator {

    @Override
    public boolean isTagSupported(String tag) {
        return "ul".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(
            final Element element,
            final DeviceCalibration calibration,
            final UserSettings settings
    ) {
        final Elements listElements = element.select("li");
        int height = settings.textSize(); // Margin
        for (final Element listElement : listElements) {
            height += calculateTextLineHeight(Jsoup.parse(listElement.html()), calibration, settings, calculateBodyPadding(settings.textSize()));
        }
        return height;
    }

}
