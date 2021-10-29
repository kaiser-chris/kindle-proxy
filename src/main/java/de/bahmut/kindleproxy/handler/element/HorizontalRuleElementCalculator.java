package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import static de.bahmut.kindleproxy.constant.CalculationConstants.FONT_SIZE;

@Component
public class HorizontalRuleElementCalculator implements ElementCalculator {

    @Override
    public boolean isTagSupported(String tag) {
        return "hr".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(Element element, DeviceCalibration calibration) {
        return FONT_SIZE + 1;
    }

}
