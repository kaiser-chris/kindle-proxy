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
            final String objectIdentifier,
            final String conditionIdentifier,
            final Class<T> targetClass
    ) {
        final CachedItem<?> item = CACHE.get(objectIdentifier + conditionIdentifier);
        if (item == null) {
            return Optional.empty();
        }
        if (item.timeToLive().isBefore(LocalDateTime.now())) {
            CACHE.remove(objectIdentifier + conditionIdentifier);
            return Optional.empty();
        }
        if (!targetClass.isInstance(item.value())) {
            return Optional.empty();
        }
        return Optional.of(targetClass.cast(item.value()));
    }

    public <T> Optional<T> getCachedItem(
            final String objectIdentifier,
            final Class<T> targetClass
    ) {
        return getCachedItem(objectIdentifier, "", targetClass);
    }

    public <T> void addItemToCache(
            final String identifier,
            final String conditionIdentifier,
            final T value,
            final Duration timeToLive
    ) {
        CACHE.put(identifier + conditionIdentifier, new CachedItem<>(identifier + conditionIdentifier, value, LocalDateTime.now().plus(timeToLive)));
    }

    public <T> void addItemToCache(
            final String objectIdentifier,
            final String conditionIdentifier,
            final T value
    ) {
        addItemToCache(objectIdentifier, conditionIdentifier, value, DEFAULT_TIME_TO_LIVE);
    }

    public <T> void addItemToCache(
            final String objectIdentifier,
            final T value
    ) {
        addItemToCache(objectIdentifier, "", value);
    }

    public void invalidItemsByConditionIdentifier(final String conditionIdentifier) {
        CACHE.keySet().stream().filter(key -> key.endsWith(conditionIdentifier)).forEach(CACHE::remove);
    }

}
