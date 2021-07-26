package de.bahmut.kindleproxy.util;

import java.util.LinkedHashMap;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StreamHelper {

    public static <T, K, U> Collector<T, ?, Map<K,U>> toOrderedMap(
            final Function<? super T, ? extends K> keyMapper,
            final Function<? super T, ? extends U> valueMapper
    ) {
        return Collectors.toMap(
                keyMapper,
                valueMapper,
                (u, v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                LinkedHashMap::new
        );
    }

}
