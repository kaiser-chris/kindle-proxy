package de.bahmut.kindleproxy.web;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.bahmut.kindleproxy.service.proxy.ProxyService;

interface ProxyBasedController {

    default Optional<ProxyService> findProxyService(final UUID id, final List<ProxyService> proxies) {
        return proxies.stream()
                .filter(proxy -> proxy.getId().equals(id))
                .findAny();
    }

}
