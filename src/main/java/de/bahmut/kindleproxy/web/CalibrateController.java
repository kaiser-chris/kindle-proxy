package de.bahmut.kindleproxy.web;

import java.util.Map;

import de.bahmut.kindleproxy.exception.CalibrationException;
import de.bahmut.kindleproxy.service.CalibrationCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Log4j2
@Controller
@RequiredArgsConstructor
public class CalibrateController {

    private final CalibrationCacheService calibrationCacheService;

    @GetMapping("/calibrate/")
    public ModelAndView calibrate(
            @RequestParam Map<String, String> allRequestParams,
            @RequestHeader("User-Agent") String agent,
            @RequestParam(value = "redirect", required = false) String redirect
    ) {
        if (allRequestParams.isEmpty() || !"true".equalsIgnoreCase(allRequestParams.get("calibrated"))) {
            return new ModelAndView("calibrate");
        }
        try {
            calibrationCacheService.cacheCalibration(agent, allRequestParams);
        } catch (final CalibrationException e) {
            log.warn("Invalid calibration", e);
            return new ModelAndView("calibrate");
        }
        if (redirect != null) {
            return new ModelAndView("redirect:" + redirect);
        } else {
            return new ModelAndView("redirect:/");
        }
    }

}
