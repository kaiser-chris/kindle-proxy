package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import de.bahmut.kindleproxy.model.UserSettings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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

    public UserSettingsService(
            HttpServletRequest request,
            HttpServletResponse response,
            CalibrationCacheService calibrationCacheService,
            CacheService cacheService
    ) {
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
            saveSettings(this.settings);
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
    }

    private String convertToCookieValue(final UserSettings settings) {
        return String.valueOf(settings.textSize());
    }

    private UserSettings convertFromCookieValue(final String cookieValue) {
        final String[] parts = cookieValue.split("\\|");
        try {
            return new UserSettings(
                    Integer.parseInt(parts[0])
            );
        } catch (final Exception e) {
            log.warn("Could not parse user settings for user " + userIdentifier + " from cookie", e);
            return new UserSettings();
        }
    }

}
