package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import org.jsoup.nodes.Element;

/**
 * Implement this interface to provide height calculation
 * for specific html elements
 */
public interface ElementCalculator {

    boolean isTagSupported(final String tag);

    int calculateElementHeight(
            final Element element,
            final DeviceCalibration calibration,
            final UserSettings settings
    );

}
