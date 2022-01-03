package dev.ebullient.pockets;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Coinage {
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
}
