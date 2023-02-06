package dev.ebullient.pockets;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactoryBuilder;
import com.github.slugify.Slugify;

import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.jackson.ObjectMapperCustomizer;

@Singleton
public class Transform {
    public final static ObjectMapper JSON;

    static {
        JSON = configure(new ObjectMapper());
    }

    static ObjectMapper configure(ObjectMapper mapper) {
        mapper.coercionConfigDefaults()
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsEmpty);

        return mapper.enable(Feature.IGNORE_UNKNOWN)
                .setSerializationInclusion(Include.NON_EMPTY)
                .setVisibility(VisibilityChecker.Std.defaultInstance()
                        .with(JsonAutoDetect.Visibility.ANY));
    }

    @Singleton
    @Produces
    // Replaces the CDI producer for ObjectMapper built into Quarkus
    ObjectMapper objectMapper(Instance<ObjectMapperCustomizer> customizers) {
        // Apply all ObjectMapperCustomizer beans (incl. Quarkus)
        for (ObjectMapperCustomizer customizer : customizers) {
            customizer.customize(JSON);
        }
        return JSON;
    }

    public static String sizeOrNull(Map<String, ?> map) {
        return map == null ? "null" : "" + map.size();
    }

    private static Slugify slugify;

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
        return text == null ? null : slugifier().slugify(text);
    }

    public static ObjectMapper mapper(Path p) {
        return p.getFileName().toString().endsWith(".json") ? Transform.JSON : Transform.yamlMapper();
    }

    private static ObjectMapper yamlMapper;

    private static ObjectMapper yamlMapper() {
        if (yamlMapper == null) {
            DumperOptions options = new DumperOptions();
            options.setDefaultScalarStyle(ScalarStyle.PLAIN);
            options.setDefaultFlowStyle(FlowStyle.AUTO);
            options.setPrettyFlow(true);

            yamlMapper = configure(new ObjectMapper(new YAMLFactoryBuilder(new YAMLFactory())
                    .dumperOptions(options).build()));
        }
        return yamlMapper;
    }

    public static String toJsonString(Object o) {
        if (o == null) {
            return null;
        }
        try {
            return JSON.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            Tui.errorf(e, "Unable to write %s to string", o);
            return o.toString();
        }
    }

    public static <T> JsonNode toJson(T obj) {
        if (obj == null) {
            return null;
        }
        return JSON.valueToTree(obj);
    }

    /**
     * Try to convert a line to a Long.
     * Do not warn if the input value could not be converted
     *
     * @see #toLong(String, PocketTui)
     */
    public static Optional<Long> toLong(String line) {
        return toLong(line, null);
    }

    /**
     * Try to convert a line to a Long.
     * Use the provided tui to emit a warning if the input value could not be converted
     *
     * @param line String to convert to a Long
     * @param tui PocketTui to use to issue a warning message; may be null.
     * @return Optional<Long> containing the value or empty if conversion failed.
     */
    public static Optional<Long> toLong(String line, PocketTui tui) {
        try {
            long value = Long.parseLong(line);
            return Optional.of(value);
        } catch (NumberFormatException ignored) {
            if (tui != null) {
                tui.warnf("Unable to determine value from the specified string: %s%n", line);
            }
        }
        return Optional.empty();
    }

    public static double toLongOrDefault(String line, long defaultValue, PocketTui tui) {
        return line.isBlank()
                ? defaultValue
                : toLong(line, tui).orElse(defaultValue);
    }

    public static Optional<Double> toDouble(String line, PocketTui tui) {
        try {
            return Optional.of(Double.parseDouble(line));
        } catch (NumberFormatException ignored) {
            if (tui != null) {
                tui.warnf("Unable to determine value from the specified string: %s%n", line);
            }
        }
        return Optional.empty();
    }

    public static double toDoubleOrDefault(String line, Double defaultValue, PocketTui tui) {
        return line.isBlank()
                ? defaultValue
                : toDouble(line, tui).orElse(defaultValue);
    }

    public static boolean toBooleanOrDefault(String line, boolean defaultValue) {
        if (line.isBlank()) {
            return defaultValue;
        }
        char first = Character.toLowerCase(line.charAt(0));
        return first == 'y' || first == 't';
    }

    public static boolean isBlank(String test) {
        return test == null || test.isBlank() || "[]".equals(test) || "{}".equals(test);
    }

    public static String toStringOrEmpty(Object o) {
        return o == null ? "" : o.toString();
    }

    public static boolean isEmpty(Object thing) {
        return thing == null || isBlank(thing.toString());
    }

    public static boolean isTrue(Boolean thing) {
        return thing != null && thing;
    }

    public static boolean isFalse(Boolean thing) {
        return thing == null || !thing;
    }

    public static <T> Iterable<T> toIterable(Iterator<T> iterator) {
        return () -> iterator;
    }
}
