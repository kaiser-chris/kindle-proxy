package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@SessionScope
public class UserSettingsService {

    private static final String COOKIE_USER_SESSION = "user-session";
    private static final String COOKIE_USER_SETTINGS = "user-settings";

    @Getter
    private final UUID userIdentifier;

    @Getter
    private UserSettings settings;

    private final HttpServletResponse response;

    private final CalibrationCacheService calibrationCacheService;

    private final CacheService cacheService;

    private final int defaultFontSize;
    private final boolean defaultShowFooter;

    public UserSettingsService(
            HttpServletRequest request,
            HttpServletResponse response,
            CalibrationCacheService calibrationCacheService,
            CacheService cacheService,
            @Value("${settings.default.font-size}") int defaultFontSize,
            @Value("${settings.default.show-footer}") boolean defaultShowFooter
    ) {
        this.defaultFontSize = defaultFontSize;
        this.defaultShowFooter = defaultShowFooter;
        final Cookie userIdentifierCookie = WebUtils.getCookie(request, COOKIE_USER_SESSION);
        final Optional<UUID> userIdentifier = getUserIdentifierFromCookie(userIdentifierCookie);
        if (userIdentifier.isPresent()) {
            this.userIdentifier = userIdentifier.get();
        } else {
            this.userIdentifier = UUID.randomUUID();
            final Cookie cookie = new Cookie(COOKIE_USER_SESSION, this.userIdentifier.toString());
            response.addCookie(cookie);
        }
        this.calibrationCacheService = calibrationCacheService;
        this.response = response;
        this.cacheService = cacheService;
        final Cookie settingsCookie = WebUtils.getCookie(request, COOKIE_USER_SETTINGS);
        final Optional<UserSettings> settings = getSettingsFromCookie(settingsCookie);
        if (settings.isPresent()) {
            this.settings = settings.get();
        } else {
            this.settings = new UserSettings();
            saveSettings(initializeSettings());
        }
    }

    private Optional<UUID> getUserIdentifierFromCookie(final Cookie cookie) {
        if (cookie == null) {
            return Optional.empty();
        }
        final String userIdentifier = cookie.getValue();
        if (userIdentifier == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(userIdentifier));
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<UserSettings> getSettingsFromCookie(final Cookie cookie) {
        if (cookie == null) {
            return Optional.empty();
        }
        final String settings = cookie.getValue();
        if (settings == null) {
            return Optional.empty();
        }
        return Optional.of(convertFromCookieValue(settings));
    }

    public void cacheCalibration(final DeviceCalibration calibration) throws CalibrationException {
        calibrationCacheService.cacheCalibration(userIdentifier, calibration);
    }

    public Optional<DeviceCalibration> getCalibration() throws CalibrationException {
        return calibrationCacheService.findCalibration(userIdentifier);
    }

    public void saveSettings(final UserSettings settings) {
        if (this.settings.equals(settings)) {
            return;
        }
        final Cookie cookie = new Cookie(COOKIE_USER_SETTINGS, convertToCookieValue(settings));
        response.addCookie(cookie);
        this.settings = settings;
        cacheService.invalidItemsByConditionIdentifier(userIdentifier.toString());
        try {
            if (calibrationCacheService.findCalibration(userIdentifier).isPresent()) {
                calibrationCacheService.deleteCalibration(userIdentifier);
            }
        } catch (final CalibrationException e) {
            throw new SettingsException("Could not remove outdated calibration of user: " + userIdentifier, e);
        }
    }

    private String convertToCookieValue(final UserSettings settings) {
        return settings.textSize() + "|" + settings.footer();
    }

    private UserSettings convertFromCookieValue(final String cookieValue) {
        final String[] parts = cookieValue.split("\\|");
        try {
            return switch (parts.length) {
                case 1 -> new UserSettings(
                        Integer.parseInt(parts[0]),
                        defaultShowFooter
                );
                case 2 -> new UserSettings(
                        Integer.parseInt(parts[0]),
                        Boolean.parseBoolean(parts[1])
                );
                default -> initializeSettings();
            };
        } catch (final Exception e) {
            log.warn("Could not parse user settings for user " + userIdentifier + " from cookie", e);
            return initializeSettings();
        }
    }

    private UserSettings initializeSettings() {
        return new UserSettings(
                defaultFontSize,
                defaultShowFooter
        );
    }

}
