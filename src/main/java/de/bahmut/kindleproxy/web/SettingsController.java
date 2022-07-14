package de.bahmut.kindleproxy.web;

import java.net.URI;
import java.net.URISyntaxException;

import de.bahmut.kindleproxy.model.UserSettings;
import de.bahmut.kindleproxy.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.web.util.UriUtils.encode;

@Log4j2
@Controller
@RequiredArgsConstructor
public class SettingsController {

    public static final String URL_SETTINGS = "/settings";

    private final UserSettingsService settingsService;

    @GetMapping("/settings")
    public ModelAndView showSettings(
            @RequestParam(value = "source", required = false) String sourceUrl
    ) {
        final var view = new ModelAndView("settings");
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
        final var view = new ModelAndView("settings");
        view.addObject("calibrate", CalibrateController.getCalibrationUrl(URL_SETTINGS));
        view.addObject("settings", settings);
        view.addObject("source", getSourceUrl(sourceUrl));
        view.addObject("isSaved", true);
        return view;
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
