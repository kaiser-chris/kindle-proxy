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

    @DisplayName("Verify first chapter of 'A Jaded Life' can be parsed")
    @Test
    void parseJadedLifeFirstChapter() throws ProxyException {
        testChapter(
                proxy,
                "19459/a-jaded-life",
                "234912/chapter-1",
                "A Jaded Life",
                "Chapter 1",
                "be here, Jill"
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