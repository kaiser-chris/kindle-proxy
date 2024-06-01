package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;

import static de.bahmut.kindleproxy.util.PageSpacingCalculator.calculateBodyPadding;
import static de.bahmut.kindleproxy.util.PageSpacingCalculator.calculateLineHeight;

@Log4j2
abstract class AbstractLineCalculator implements ElementCalculator {

    protected int calculateTextLineHeight(
            final Document htmlLine,
            final DeviceCalibration calibration,
            final UserSettings settings
    ) {
        return calculateTextLineHeight(htmlLine, calibration, settings, 0);
    }

    protected int calculateTextLineHeight(
            final Document htmlLine,
            final DeviceCalibration calibration,
            final UserSettings settings,
            final int padding
    ) {
        final String cleanLine = htmlLine.text();
        if (cleanLine.isEmpty() && !htmlLine.toString().isEmpty() && htmlLine.getElementsByTag("img").isEmpty()) {
            return (int) Math.ceil(calculateLineHeight(settings.textSize()));
        }
        int currentLine = 1;
        double currentLineWidth = calculateBodyPadding(settings.textSize()) + padding;
        double splitWidth = calibration.calculateCharacterWidth(' ', settings.textSize());
        for (final String word : cleanLine.split(" ")) {
            double wordWidth = calculateWordWidth(word, calibration, settings);
            wordWidth = wordWidth * 0.85; //TODO: Hack fix
            if ((currentLineWidth - (2 * splitWidth) + wordWidth) > calibration.width()) {
                currentLine++;
                currentLineWidth = calculateBodyPadding(settings.textSize());
            }
            currentLineWidth += wordWidth;
        }
        log.debug("{} Lines in: {}", currentLine, cleanLine);
        return (int) Math.ceil(currentLine * calculateLineHeight(settings.textSize()));
    }

    private double calculateWordWidth(
            final String word,
            final DeviceCalibration calibration,
            final UserSettings settings
    ) {
        double wordWidth = calibration.calculateCharacterWidth(' ', settings.textSize());
        for (char character : word.toCharArray()) {
            wordWidth += calibration.calculateCharacterWidth(character, settings.textSize());
        }
        return wordWidth;
    }

}
