package de.bahmut.kindleproxy.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PageSpacingCalculator {

    public static final int LIST_PADDING = 40;

    public static int calculateBodyPadding(final int fontSize) {
        return  2 * 2 * fontSize;
    }

    public static double calculateLineHeight(final int fontSize) {
        return Math.floor(fontSize * 1.4);
    }

}
