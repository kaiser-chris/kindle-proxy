package de.bahmut.kindleproxy.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

@Service
public class TemplateCacheService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String getTemplate(final String name) {
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        final ClassLoader classLoader = getClass().getClassLoader();
        try (final InputStream stream = classLoader.getResourceAsStream("template/" + name + ".html")) {
            if (stream == null) {
                //TODO
                throw new RuntimeException("File not found");
            }
            final String templateContent = IOUtils.toString(stream, StandardCharsets.UTF_8);
            cache.put(name, templateContent);
            return templateContent;
        } catch (final IOException e) {
            //TODO
            throw new RuntimeException(e);
        }
    }

}
