package de.bahmut.kindleproxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    private String identifier;

    private String bookIdentifier;

    private String title;

    private String body;

    private String nextChapterIdentifier;

    private String previousChapterIdentifier;

}
