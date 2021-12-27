package dev.ebullient.pockets;

import java.util.Optional;

import com.github.slugify.Slugify;

import picocli.CommandLine.Option;

public class Input {

    private static Slugify slugify;

    public static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = slugify = new Slugify().withLowerCase(true);
        }
        return s;
    }

    public static Optional<Long> getId(String nameOrId) {
        try {
            long id = Long.parseLong(nameOrId);
            return Optional.of(id);
        } catch (NumberFormatException nfe) {
        }
        return Optional.empty();
    }

    public static boolean yesOrTrue(String line, boolean defaultValue) {
        if (!line.isBlank()) {
            char first = Character.toLowerCase(line.charAt(0));
            if (first == 'y' || first == 't') {
                return true;
            } else {
                return false;
            }
        }
        return defaultValue;
    }

    public static class PocketAttributes {
        @Option(names = { "-c", "--capacity" }, description = "Capacity: Maximum this pocket can contain in pounds")
        Optional<Double> max_capacity = Optional.empty();

        @Option(names = { "-v", "--volume" }, description = "Volume: Maximum this pocket can contain in cubic feet")
        Optional<Double> max_volume = Optional.empty();

        @Option(names = { "-w", "--weight" }, description = "Weight of the pocket itself")
        Optional<Double> weight = Optional.empty();

        @Option(names = { "-m",
                "--magic" }, negatable = true, defaultValue = "false", description = "Is this a magic pocket? Magic pockets always weigh the same, regardless of their contents")
        boolean magic = false;
    }
}
