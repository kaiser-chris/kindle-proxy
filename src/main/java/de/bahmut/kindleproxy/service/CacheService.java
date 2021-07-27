package de.bahmut.kindleproxy.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import de.bahmut.kindleproxy.model.CachedItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Duration DEFAULT_TIME_TO_LIVE = Duration.ofMinutes(60);
    private static final Map<String, CachedItem<?>> CACHE = new ConcurrentHashMap<>();

    public <T> Optional<T> getCachedItem(
            final String identifier,
            final Class<T> targetClass
    ) {
        final CachedItem<?> item = CACHE.get(identifier);
        if (item == null) {
            return Optional.empty();
        }
        if (item.timeToLive().isBefore(LocalDateTime.now())) {
            CACHE.remove(identifier);
            return Optional.empty();
        }
        if (!targetClass.isInstance(item.value())) {
            return Optional.empty();
        }
        return Optional.of(targetClass.cast(item.value()));
    }

    public <T> void addItemToCache(
            final String identifier,
            final T value,
            final Duration timeToLive
    ) {
        CACHE.put(identifier, new CachedItem<>(identifier, value, LocalDateTime.now().plus(timeToLive)));
    }

    public <T> void addItemToCache(
            final String identifier,
            final T value
    ) {
        addItemToCache(identifier, value, DEFAULT_TIME_TO_LIVE);
    }

}
