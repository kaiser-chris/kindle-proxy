package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

class ScriptCleanerTest {

    private static final String TEST_HTML =
            """
            <html>
                <head>
                    <script>let someScript=1</script>
                </head>
                <body>
                    <script>let someScript=1</script>
                    <span>Content</span>
                    <script>let someScript=1</script>
                </body>
                <script>let someScript=1</script>
            </html>
            <script>let someScript=1</script>
            """;

    private ScriptCleaner cleaner;
    private Document testHtml;

    @BeforeEach
    void setUp() {
        cleaner = new ScriptCleaner();
        testHtml = Jsoup.parse(TEST_HTML);
    }

    @DisplayName("Verify that script tags are removed properly")
    @Test
    void testCleaner() {
        final Document cleaned = cleaner.clean(testHtml);
        final String cleanedHtml = cleaned.html();
        assertThat(cleanedHtml, not(containsString("script")));
        assertThat(cleanedHtml, containsString("<span>Content</span>"));
    }

}