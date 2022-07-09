package de.bahmut.kindleproxy.web;

import de.bahmut.kindleproxy.model.UserSettings;

interface RenderingController {

    default String contentStyle(final UserSettings settings) {
        return "font-size: " +
                settings.textSize() +
                "px !important;";
    }

}
