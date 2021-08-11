package de.bahmut.kindleproxy.util.check;

import de.bahmut.kindleproxy.exception.ProxyException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProxyConditions {

    public static void checkProxyResult(final boolean check, final String message) throws ProxyException {
        if (!check) {
            return;
        }
        throw new ProxyException(message);
    }

    public static void checkProxyResult(final boolean check, final String message, final Object... inserts) throws ProxyException {
        checkProxyResult(check, String.format(message, inserts));
    }

}
