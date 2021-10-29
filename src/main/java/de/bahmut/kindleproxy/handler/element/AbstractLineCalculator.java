package de.bahmut.kindleproxy.handler.element;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import lombok.extern.log4j.Log4j2;
import org.jsoup.nodes.Document;

import static de.bahmut.kindleproxy.constant.CalculationConstants.BODY_PADDING;
import static de.bahmut.kindleproxy.constant.CalculationConstants.FONT_SIZE;
import static de.bahmut.kindleproxy.constant.CalculationConstants.LINE_HEIGHT;

@Log4j2
abstract class AbstractLineCalculator implements ElementCalculator {

    protected int calculateTextLineHeight(
            final Document htmlLine,
            final DeviceCalibration calibration
    ) {
        return calculateTextLineHeight(htmlLine, calibration, 0);
    }

    protected int calculateTextLineHeight(
            final Document htmlLine,
            final DeviceCalibration calibration,
            final int padding
    ) {
        final String cleanLine = htmlLine.text();
        if (cleanLine.isEmpty() && !htmlLine.toString().isEmpty() && htmlLine.getElementsByTag("img").isEmpty()) {
            return (int) Math.ceil(LINE_HEIGHT);
        }
        int currentLine = 1;
        double currentLineWidth = BODY_PADDING + padding;
        double splitWidth = calibration.calculateCharacterWidth(' ', FONT_SIZE);
        for (final String word : cleanLine.split(" ")) {
            double wordWidth = calculateWordWidth(word, calibration);
            wordWidth = wordWidth * 0.85; //TODO: Hack fix
            if ((currentLineWidth - (2 * splitWidth) + wordWidth) > calibration.width()) {
                currentLine++;
                currentLineWidth = BODY_PADDING;
            }
            currentLineWidth += wordWidth;
        }
        log.debug(currentLine + " Lines in: " + cleanLine);
        return (int) Math.ceil(currentLine * LINE_HEIGHT);
    }

    private double calculateWordWidth(final String word, final DeviceCalibration calibration) {
        double wordWidth = calibration.calculateCharacterWidth(' ', FONT_SIZE);
        for (char character : word.toCharArray()) {
            wordWidth += calibration.calculateCharacterWidth(character, FONT_SIZE);
        }
        return wordWidth;
    }

}
