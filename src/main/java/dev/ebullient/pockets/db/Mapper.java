package dev.ebullient.pockets.db;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.slugify.Slugify;

import dev.ebullient.pockets.io.PocketTui;

public class Mapper {
    private static Slugify slugify;

    private static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = slugify = new Slugify()
                    .withCustomReplacement("'", "")
                    .withLowerCase(true);
        }
        return s;
    }

    public static String slugify(String text) {
        return slugifier().slugify(text);
    }

    /**
     * Try to convert a line to a Long.
     * Do not warn if the input value could not be converted
     * @see #toLong(String, PocketTui)
     */
    public static Optional<Long> toLong(String line) {
        return toLong(line, null);
    }

    /**
     * Try to convert a line to a Long.
     * Use the provided tui to emit a warning if the input value could not be converted
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

    public static double toDoubleOrDefault(String line, double defaultValue, PocketTui tui) {
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

    public static class Currency {
        static final Pattern COIN_VALUE = Pattern.compile("([0-9]*\\.?[0-9]+)([pgesc]p)");

        enum Coin {
            cp(new double[] { 1, 0.10, 0.02, 0.01, 0.001 }),
            sp(new double[] { 10, 1, 0.2, 0.1, 0.01 }),
            ep(new double[] { 50, 5, 1, 0.5, 0.05 }),
            gp(new double[] { 100, 10, 2, 1, 0.1 }),
            pp(new double[] { 1000, 100, 20, 10, 1 });

            double cpEx, spEx, epEx, gpEx, ppEx;

            Coin(double[] exchangeRates) {
                cpEx = exchangeRates[0];
                spEx = exchangeRates[1];
                epEx = exchangeRates[2];
                gpEx = exchangeRates[3];
                ppEx = exchangeRates[4];
            }
        }

        public static Optional<Double> gpValue(String line) {
            if (line == null || line.isBlank()) {
                return Optional.empty();
            }
            Matcher m = COIN_VALUE.matcher(line);
            if (m.matches()) {
                double amount = Double.parseDouble(m.group(1)); // amount
                switch (m.group(2)) { // coin
                    case "cp":
                        return Optional.of(Coin.cp.gpEx * amount);
                    case "sp":
                        return Optional.of(Coin.sp.gpEx * amount);
                    case "ep":
                        return Optional.of(Coin.ep.gpEx * amount);
                    case "gp":
                        return Optional.of(amount);
                    case "pp":
                        return Optional.of(Coin.pp.gpEx * amount);
                }
            }
            return Optional.empty(); // no match
        }

        public static Optional<Double> gpValue(String line, PocketTui tui) {
            Optional<Double> gpValue = Currency.gpValue(line);
            if (gpValue.isEmpty() && tui != null) {
                tui.warnf("Unable to determine value from the specified string: %s%n", line);
                tui.outPrintln("A value should be specified as a decimal number and a unit, e.g. 1gp or 0.1pp");
            }
            return gpValue;
        }

        public static double gpValueOrDefault(String line, Double defaultValue) {
            return gpValueOrDefault(line, defaultValue, null);
        }

        public static double gpValueOrDefault(String line, Double defaultValue, PocketTui tui) {
            return line.isBlank()
                    ? defaultValue
                    : gpValue(line, tui).orElse(defaultValue);
        }
    }
}
