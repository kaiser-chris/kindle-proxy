package de.bahmut.kindleproxy.web;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import de.bahmut.kindleproxy.exception.SettingsException;
import de.bahmut.kindleproxy.model.UserSettings;
import de.bahmut.kindleproxy.service.UserSettingsService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static de.bahmut.kindleproxy.util.RenderingUtils.sizeContentStyle;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encode;

@Log4j2
@Controller
public class SettingsController {

    public static final String URL_SETTINGS = "/settings";

    private final UserSettingsService settingsService;
    private final Integer[] fontSizeScale;

    public SettingsController(
            UserSettingsService settingsService,
            @Value("${settings.font-size-scale}") String fontSizeScale
    ) {
        this.settingsService = settingsService;
        try {
            this.fontSizeScale = Arrays.stream(fontSizeScale.split(","))
                    .filter(Objects::nonNull)
                    .map(String::strip)
                    .map(Integer::parseInt)
                    .toArray(Integer[]::new);
            if (this.fontSizeScale.length != 10) {
                throw new SettingsException("Could not parse font size scale: List has not exactly 10 entries");
            }
        } catch (final NumberFormatException e) {
            throw new SettingsException("Could not parse font size scale: " + e.getMessage(), e);
        }
    }

    @GetMapping("/settings")
    public ModelAndView showSettings(
            @RequestParam(value = "source", required = false) String sourceUrl
    ) {
        final var view = createSettingsModelAndView();
        view.addObject("calibrate", CalibrateController.getCalibrationUrl(URL_SETTINGS));
        view.addObject("settings", settingsService.getSettings());
        view.addObject("source", getSourceUrl(sourceUrl));
        view.addObject("isSaved", false);
        return view;
    }

    @PostMapping("/settings")
    public ModelAndView saveSettings(
            @ModelAttribute UserSettings settings,
            @RequestParam("source") String sourceUrl
    ) {
        settingsService.saveSettings(settings);
        final var view = createSettingsModelAndView();
        view.addObject("calibrate", CalibrateController.getCalibrationUrl(URL_SETTINGS));
        view.addObject("settings", settings);
        view.addObject("source", getSourceUrl(sourceUrl));
        view.addObject("isSaved", true);
        return view;
    }

    private ModelAndView createSettingsModelAndView() {
        final var modelAndView = new ModelAndView();
        modelAndView.setViewName("settings");
        modelAndView.addObject("contentStyle", sizeContentStyle(settingsService.getSettings()));
        modelAndView.addObject("fontSizeScale", renderFontSizeScale(fontSizeScale));
        return modelAndView;
    }

    private String getSourceUrl(final String sourceUrl) {
        URI inputUri;
        try {
            if (sourceUrl != null) {
                inputUri = new URI(sourceUrl);
            } else {
                inputUri = null;
            }
        } catch (final URISyntaxException e) {
            inputUri = null;
        }
        if (inputUri != null && !inputUri.isAbsolute()) {
            return sourceUrl;
        } else {
            return "/";
        }
    }

    private String renderFontSizeScale(final Integer[] scale) {
        return Arrays.stream(scale)
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    public static String getSettingsUrl(
            final String sourceUrl
    ) {
        if (sourceUrl == null) {
            return "/settings";
        } else {
            return "/settings?source=" + encode(sourceUrl, UTF_8);
        }
    }

}
