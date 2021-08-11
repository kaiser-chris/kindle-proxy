package de.bahmut.kindleproxy.model;

import java.time.LocalDateTime;

public record CachedItem<T>(
        String identifier,
        T value,
        LocalDateTime timeToLive
) {

}