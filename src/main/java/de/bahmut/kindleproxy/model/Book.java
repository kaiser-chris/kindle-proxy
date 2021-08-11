package de.bahmut.kindleproxy.model;

import java.util.List;

public record Book(
        String identifier,
        String name,
        List<Reference> chapters
) {

}