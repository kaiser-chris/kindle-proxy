package de.bahmut.kindleproxy.web;

import java.util.HashMap;
import java.util.Map;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.service.UserSettingsService;
import de.bahmut.kindleproxy.web.base.RenderingController;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.springframework.web.util.UriUtils.encode;

@Log4j2
@Controller
@RequiredArgsConstructor
public class CalibrateController implements RenderingController {

    private static final double DEFAULT_RATIO = 0.9;

    private final UserSettingsService settingsService;

    @GetMapping("/calibrate/")
    public ModelAndView calibrate(
            @RequestParam Map<String, String> allRequestParams,
            @RequestParam(value = "redirect", required = false) String redirect
    ) {
        if (allRequestParams.isEmpty() || !"true".equalsIgnoreCase(allRequestParams.get("calibrated"))) {
            return new ModelAndView("calibrate");
        }
        try {
            final DeviceCalibration calibration = buildCalibration(allRequestParams);
            settingsService.cacheCalibration(calibration);
        } catch (final CalibrationException e) {
            log.warn("Invalid calibration", e);
            final var webPage = new ModelAndView("calibrate");
            webPage.addObject("contentStyle", contentStyle(settingsService.getSettings()));
            return webPage;
        }
        if (redirect != null) {
            return new ModelAndView("redirect:" + redirect);
        } else {
            return new ModelAndView("redirect:/");
        }
    }

    public static String getCalibrationUrl(
            final String redirectUrl
    ) {
        if (redirectUrl == null) {
            return "/calibrate/";
        } else {
            return "/calibrate/?redirect=" + encode(redirectUrl, UTF_8);
        }
    }

    private DeviceCalibration buildCalibration(
            final Map<String, String> queryParameters
    ) throws CalibrationException {
        final Map<Character, Double> characters = new HashMap<>();
        for (final Map.Entry<String, String> entry : queryParameters.entrySet()) {
            if (entry.getKey().length() != 1) {
                continue;
            }
            final char character = entry.getKey().charAt(0);
            final double ratio;
            try {
                ratio = Double.parseDouble(entry.getValue());
            } catch(final NumberFormatException e) {
                throw new CalibrationException("Could not parse character ratio for char " + character, e);
            }
            characters.put(character, ratio);
        }
        final double defaultRatio;
        if (characters.get('M') != null) {
            defaultRatio = characters.get('M');
        } else {
            defaultRatio = DEFAULT_RATIO;
        }
        final int width;
        final int height;
        try {
            width = Integer.parseInt(queryParameters.get("width"));
            height = Integer.parseInt(queryParameters.get("height"));
        } catch(final NumberFormatException e) {
            throw new CalibrationException("Could not parse resolution", e);
        }
        return new DeviceCalibration(width, height, defaultRatio, characters);
    }

}
