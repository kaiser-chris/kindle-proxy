package de.bahmut.kindleproxy.service;

import de.bahmut.kindleproxy.constant.Device;
import org.springframework.stereotype.Service;

@Service
public class DeviceDetectionService {

    public Device detectDevice() {
        return Device.KINDLE_PAPERWHITE;
    }

}
