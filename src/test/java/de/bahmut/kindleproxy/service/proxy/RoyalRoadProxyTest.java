package de.bahmut.kindleproxy.service.proxy;

import de.bahmut.kindleproxy.exception.ProxyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RoyalRoadProxyTest extends AbstractProxyImplementationTest {

    @Autowired
    private RoyalRoadProxy proxy;

    @DisplayName("Verify first chapter of 'Beneath the Dragoneye Moons' can be parsed")
    @Test
    void parseBtdemFirstChapter() throws ProxyException {
        testChapter(
                proxy,
                "36299/beneath-the-dragoneye-moons",
                "561246/chapter-1-rebirth",
                "Beneath the Dragoneye Moons",
                "Chapter 1 - Rebirth",
                "galaxies"
        );
    }

    @DisplayName("Verify first chapter of 'Paranoid Mage' can be parsed")
    @Test
    void parseParanoidMageFirstChapter() throws ProxyException {
        testChapter(
                proxy,
                "49879/paranoid-mage",
                "816456/chapter-1-revelation",
                "Paranoid Mage",
                "Chapter 1 - Revelation",
                "Callum Wells"
        );
    }
}