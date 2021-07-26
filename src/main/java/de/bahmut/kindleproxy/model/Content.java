package de.bahmut.kindleproxy.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {

    private String title;

    private String body;

    private String nextContent;

    private String previousContent;

}
