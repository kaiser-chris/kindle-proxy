package de.bahmut.kindleproxy.web;

import java.net.URL;
import java.util.Map;

import de.bahmut.kindleproxy.service.CalibrationCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class CalibrateController {

    private final CalibrationCacheService calibrationCacheService;

    @GetMapping("/calibrate/")
    public String calibrate(
            @RequestParam Map<String, String> allRequestParams,
            @RequestHeader("User-Agent") String agent,
            @RequestParam(value = "redirect", required = false) String redirect
    ) {
        if (allRequestParams.isEmpty() || !"true".equalsIgnoreCase(allRequestParams.get("calibrated"))) {
            return "calibrate";
        }
        calibrationCacheService.cacheCalibration(agent, allRequestParams);
        if (redirect != null) {
            return "redirect:" + redirect;
        } else {
            return "redirect:/";
        }
    }

}
