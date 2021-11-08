package de.bahmut.kindleproxy.handler.cleaner;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class TableAbsoluteWidthCleanerTest {

    private static final String TEST_HTML =
            """
            <html>
                <body>
                    <table width="100">
                        <thead width="100%">
                            <tr width="15">
                                <th width="50">Head</th>
                            </tr>
                        </thead>
                        <tbody width="100%">
                            <tr width="15">
                                <td width="50">Body1</td>
                            </tr>
                            <tr width="15">
                                <td width="50%">Body2</td>
                            </tr>
                        </tbody>
                    </table>
                </body>
            </html>
            """;

    private TableAbsoluteWidthCleaner cleaner;
    private Document testHtml;

    @BeforeEach
    void setUp() {
        cleaner = new TableAbsoluteWidthCleaner();
        testHtml = Jsoup.parse(TEST_HTML);
    }

    @DisplayName("Verify that absolute values in table width attributes are removed properly and percentage values are not removed")
    @Test
    void testCleaner() {
        final Document cleaned = cleaner.clean(testHtml);
        final String cleanedHtml = cleaned.html();
        assertThat(cleanedHtml, containsString("<tr>"));
        assertThat(cleanedHtml, containsString("<thead width=\"100%\">"));
        assertThat(cleanedHtml, containsString("<th>Head</th>"));
        assertThat(cleanedHtml, containsString("<td>Body1</td>"));
        assertThat(cleanedHtml, containsString("<td width=\"50%\">Body2</td>"));
        assertThat(cleanedHtml, containsString("<tbody width=\"100%\">"));
    }

}