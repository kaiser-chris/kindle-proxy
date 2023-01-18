package de.bahmut.kindleproxy.model;

import java.util.List;

public record Proxy(
        String identifier,
        String name,
        List<Book> books
) {

}
