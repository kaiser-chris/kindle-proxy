package de.bahmut.kindleproxy.handler.element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ImageElementCalculator implements ElementCalculator {

    @Override
    public boolean isTagSupported(String tag) {
        return "img".equalsIgnoreCase(tag);
    }

    @Override
    public int calculateElementHeight(
            final Element imageElement,
            final DeviceCalibration calibration,
            final UserSettings settings
    ) {
        final String heightValue = imageElement.attributes().get("height");
        if (StringUtils.isNotBlank(heightValue)) {
            return Integer.parseInt(heightValue);
        }
        final String urlValue = imageElement.attributes().get("src");
        final String widthValue = imageElement.attributes().get("width");
        if (StringUtils.isBlank(urlValue)) {
            return 0;
        }
        final URL url;
        try {
            url = new URL(urlValue);
        } catch (final MalformedURLException e) {
            log.warn("Could not download embedded image because of an invalid url", e);
            return 0;
        }
        try (final InputStream stream = url.openStream()) {
            BufferedImage image = ImageIO.read(stream);
            if (StringUtils.isNotBlank(widthValue)) {
                final double ratio = Double.parseDouble(widthValue) / (double) image.getWidth();
                return (int) (image.getHeight() * ratio);
            } else {
                return image.getHeight();
            }
        } catch (final IOException e) {
            log.warn("Could not download embedded image", e);
            return settings.textSize();
        }
    }

}
