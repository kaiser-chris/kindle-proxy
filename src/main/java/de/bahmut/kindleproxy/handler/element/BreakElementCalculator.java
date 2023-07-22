package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class BreakElementCalculator extends AbstractLineCalculator {

    @Override
    public boolean isTagSupported(String tag) {
        return "br".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(
            Element element,
            DeviceCalibration calibration,
            UserSettings settings
    ) {
        return settings.getTextSize();
    }

}
