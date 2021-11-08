package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class TextColorCleanerTest {

    private static final String TEST_HTML =
            """
            <html>
                <body>
                    <span style="color: red">Red Content</span>
                    <font color="blue">Blue Content</font>
                </body>
            </html>
            """;

    private TextColorCleaner cleaner;
    private Document testHtml;

    @BeforeEach
    void setUp() {
        cleaner = new TextColorCleaner();
        testHtml = Jsoup.parse(TEST_HTML);
    }

    @DisplayName("Verify that the style and color attribute are removed properly")
    @Test
    void testCleaner() {
        final Document cleaned = cleaner.clean(testHtml);
        final String cleanedHtml = cleaned.html();
        assertThat(cleanedHtml, containsString("<span>Red Content</span>"));
        assertThat(cleanedHtml, containsString("<font>Blue Content</font>"));
    }

}