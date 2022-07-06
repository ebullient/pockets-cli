package dev.ebullient.pockets.db;

import java.util.Optional;

import com.github.slugify.Slugify;

import dev.ebullient.pockets.io.PocketTui;

public class Mapper {
    private static Slugify slugify;

    private static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = slugify = Slugify.builder()
                    .customReplacement("'", "")
                    .lowerCase(true)
                    .build();
        }
        return s;
    }

    public static String slugify(String text) {
        return slugifier().slugify(text);
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

}
