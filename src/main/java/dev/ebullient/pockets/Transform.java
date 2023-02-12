package dev.ebullient.pockets;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.github.slugify.Slugify;

import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public class Transform {
    public final static ObjectMapper FROM_JSON = new ObjectMapper()
            .setVisibility(VisibilityChecker.Std.defaultInstance()
                    .with(JsonAutoDetect.Visibility.ANY));

    @Singleton
    @Produces
    // Replaces the CDI producer for ObjectMapper built into Quarkus
    ObjectMapper objectMapper(Instance<ObjectMapperCustomizer> customizers) {
        // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
        for (ObjectMapperCustomizer customizer : customizers) {
            customizer.customize(FROM_JSON);
        }
        return FROM_JSON;
    }

    static Slugify slugify;

    private static Slugify slugifier() {
        Slugify s = Transform.slugify;
        if (s == null) {
            s = Transform.slugify = Slugify.builder()
                    .customReplacement("'", "")
                    .lowerCase(true)
                    .build();
        }
        return s;
    }

    public static String slugify(String text) {
        return slugifier().slugify(text);
    }
}
