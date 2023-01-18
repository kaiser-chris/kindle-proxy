package de.bahmut.kindleproxy.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;

@UtilityClass
public class ListHelper {

    public <T> Optional<T> getNext(T item, List<T> list) {
        int index = list.indexOf(item);
        if (index < 0 || index + 1 == list.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index + 1));
    }

    public <T> Optional<T> getPrevious(T item, List<T> list) {
        int index = list.indexOf(item);
        if (index <= 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(index - 1));
    }

}
