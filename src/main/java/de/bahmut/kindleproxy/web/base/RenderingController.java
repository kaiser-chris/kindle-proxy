package de.bahmut.kindleproxy.web.base;

import de.bahmut.kindleproxy.model.UserSettings;

public interface RenderingController {

    default String contentStyle(final UserSettings settings) {
        return "font-size: " +
                settings.textSize() +
                "px !important;";
    }

}
