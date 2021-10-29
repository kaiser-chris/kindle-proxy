package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import static de.bahmut.kindleproxy.constant.CalculationConstants.FONT_SIZE;
import static de.bahmut.kindleproxy.constant.CalculationConstants.LIST_PADDING;

@Component
public class ListElementCalculator extends AbstractLineCalculator {

    @Override
    public boolean isTagSupported(String tag) {
        return "ul".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(Element element, DeviceCalibration calibration) {
        final Elements listElements = element.select("li");
        int height = FONT_SIZE; // Margin
        for (final Element listElement : listElements) {
            height += calculateTextLineHeight(Jsoup.parse(listElement.html()), calibration, LIST_PADDING);
        }
        return height;
    }

}
