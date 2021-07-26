package de.bahmut.kindleproxy.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Book extends BookReference {

    public Book(final String identifier, final String name, final List<ChapterReference> chapters) {
        super(identifier, name);
        this.chapters = chapters;
    }

    private List<ChapterReference> chapters;

}
