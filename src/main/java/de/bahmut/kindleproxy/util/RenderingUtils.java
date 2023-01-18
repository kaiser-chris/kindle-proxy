package de.bahmut.kindleproxy.util;

import de.bahmut.kindleproxy.model.UserSettings;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RenderingUtils {

    public static String sizeContentStyle(final UserSettings settings) {
        return "font-size: " +
                settings.textSize() +
                "px !important;";
    }

}
