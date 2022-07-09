package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class HorizontalRuleElementCalculator implements ElementCalculator {

    @Override
    public boolean isTagSupported(String tag) {
        return "hr".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(
            final Element element,
            final DeviceCalibration calibration,
            final UserSettings settings
    ) {
        return settings.textSize() + 1;
    }

}
