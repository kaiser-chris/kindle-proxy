package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.model.DeviceCalibration;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@SessionScope
public class UserSettingsService {

    @Getter
    private final UUID userIdentifier;

    private final CalibrationCacheService calibrationCacheService;

    public UserSettingsService(
            HttpServletRequest request,
            CalibrationCacheService calibrationCacheService
    ) {
        this.userIdentifier = UUID.nameUUIDFromBytes(request.getHeader("User-Agent").getBytes(StandardCharsets.UTF_8));
        this.calibrationCacheService = calibrationCacheService;
    }

    public void cacheCalibration(final DeviceCalibration calibration) throws CalibrationException {
        calibrationCacheService.cacheCalibration(userIdentifier, calibration);
    }

    public Optional<DeviceCalibration> getCalibration() throws CalibrationException {
        return calibrationCacheService.findCalibration(userIdentifier);
    }



}
