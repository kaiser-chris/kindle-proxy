package de.bahmut.kindleproxy.service.proxy;

import de.bahmut.kindleproxy.exception.ProxyException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@RequiredArgsConstructor
class WanderingInnProxyTest extends AbstractProxyImplementationTest {

    @Autowired
    private WanderingInnProxy proxy;

    @DisplayName("Verify first chapter can be parsed")
    @Test
    void parseFirstChapter() throws ProxyException {
        testChapter(
                proxy,
                "fc452c26-db3c-3aa6-b621-3b9c5d9e3abc",
                "5f6caafb-7888-364d-a4ca-a7a3d1a04493",
                "The Wandering Inn",
                "1.00",
                "Erin"
        );
    }

    @DisplayName("Verify chapter seven in volume four can be parsed")
    @Test
    void parseChapterInVolumeFour() throws ProxyException {
        testChapter(
                proxy,
                "0dabdb78-5717-3761-97a1-c2db6bf1a14b",
                "2dabf54e-8798-3a7e-80da-245c7a29eddb",
                "The Wandering Inn",
                "4.07",
                "Klbkchhezeim"
        );
    }

}