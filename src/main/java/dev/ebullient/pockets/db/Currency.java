package dev.ebullient.pockets.db;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.index.ItemReference;
import dev.ebullient.pockets.io.PocketTui;

public enum Currency {
    cp(new double[] { 1, 0.10, 0.02, 0.01, 0.001 }, "copper-cp"),
    sp(new double[] { 10, 1, 0.2, 0.1, 0.01 }, "silver-sp"),
    ep(new double[] { 50, 5, 1, 0.5, 0.05 }, "electrum-ep"),
    gp(new double[] { 100, 10, 2, 1, 0.1 }, "gold-gp"),
    pp(new double[] { 1000, 100, 20, 10, 1 }, "platinum-pp");

    public final double cpEx, spEx, epEx, gpEx, ppEx;
    public final String itemRef;

    Currency(double[] exchangeRates, String s) {
        cpEx = exchangeRates[0];
        spEx = exchangeRates[1];
        epEx = exchangeRates[2];
        gpEx = exchangeRates[3];
        ppEx = exchangeRates[4];
        itemRef = s;
    }

    static final Pattern COIN_VALUE = Pattern.compile("([0-9]*\\.?[0-9]+)([pgesc]p)");

    public static Optional<Double> cpValue(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }
        Matcher m = COIN_VALUE.matcher(line);
        if (m.matches()) {
            double amount = Double.parseDouble(m.group(1)); // amount
            switch (m.group(2)) { // coin
                case "cp":
                    return Optional.of(amount);
                case "sp":
                    return Optional.of(sp.cpEx * amount);
                case "ep":
                    return Optional.of(ep.cpEx * amount);
                case "gp":
                    return Optional.of(gp.cpEx * amount);
                case "pp":
                    return Optional.of(pp.cpEx * amount);
            }
        }
        return Optional.empty(); // no match
    }

    public static Optional<Double> cpValue(String line, PocketTui tui) {
        Optional<Double> cpValue = cpValue(line);
        if (cpValue.isEmpty() && tui != null) {
            tui.warnf("Unable to determine value from the specified string: %s%n", line);
            tui.outPrintln("A value should be specified as a decimal number and a unit, e.g. 1gp or 0.1pp");
        }
        return cpValue;
    }

    public static double cpValueOrDefault(String line, Double defaultValue) {
        return cpValueOrDefault(line, defaultValue, null);
    }

    public static double cpValueOrDefault(String line, Double defaultValue, PocketTui tui) {
        return line.isBlank()
                ? defaultValue
                : cpValue(line, tui).orElse(defaultValue);
    }

    public static class CoinItem {
        Item item;
        Currency type;

        CoinItem(Currency type, Item item) {
            this.item = item;
            this.type = type;
        }

        public void deduct() {
            item.quantity = 0;
        }

        void add(int i) {
            item.quantity += i;
        }

        int deductCpValue(double cpValue) {
            int numCoins = (int) (cpValue / type.cpEx);
            if (numCoins > item.quantity) {
                numCoins = item.quantity;
            }
            item.quantity -= numCoins;
            return (int) (numCoins * type.cpEx);
        }

        int cpValue() {
            return (int) (item.quantity * type.cpEx);
        }
    }

    public static class CoinPurse {
        final PocketTui tui;
        final CoinItem pp;
        final CoinItem gp;
        final CoinItem ep;
        final CoinItem sp;
        final CoinItem cp;

        public CoinPurse(Pocket pocket, PocketTui tui, Index index) {
            this.tui = tui;
            this.pp = getItem(pocket, Currency.pp, index);
            this.gp = getItem(pocket, Currency.gp, index);
            this.ep = getItem(pocket, Currency.ep, index);
            this.sp = getItem(pocket, Currency.sp, index);
            this.cp = getItem(pocket, Currency.cp, index);
        }

        public void add(Currency type, Integer v) {
            CoinItem coins = getCoinsOfType(type);
            coins.add(v);
        }

        public int totalCpValue() {
            int cpValue = 0;
            cpValue += pp.cpValue();
            cpValue += gp.cpValue();
            cpValue += ep.cpValue();
            cpValue += sp.cpValue();
            cpValue += cp.cpValue();
            return cpValue;
        }

        public boolean deduct(int deltaCpValue) {
            int cumulative = totalCpValue();

            if (deltaCpValue > cumulative) {
                return false;
            }
            if (deltaCpValue == cumulative) {
                pp.deduct();
                gp.deduct();
                ep.deduct();
                sp.deduct();
                cp.deduct();
                return true;
            }

            int remainder = deltaCpValue;
            remainder -= pp.deductCpValue(remainder);
            remainder -= gp.deductCpValue(remainder);
            remainder -= ep.deductCpValue(remainder);
            remainder -= sp.deductCpValue(remainder);
            remainder -= cp.deductCpValue(remainder);

            if (remainder != 0) {
                tui.debugf("Unable to deduct %s from %s", deltaCpValue, this);
                return false;
            }
            return true;
        }

        CoinItem getItem(Pocket pocket, Currency type, Index index) {
            Optional<Item> coinItem = pocket.items.stream()
                    .filter(i -> i.itemRef.equals(type.itemRef))
                    .findFirst();

            if (coinItem.isPresent()) {
                return new CoinItem(type, coinItem.get());
            }

            ItemReference iRef = index.getItemReference(type.itemRef).get();
            Item item = iRef.createItem();
            item.quantity = 0;
            item.addToPocket(pocket);
            return new CoinItem(type, item);
        }

        CoinItem getCoinsOfType(Currency coin) {
            switch (coin) {
                case pp:
                    return this.pp;
                default:
                case gp:
                    return this.gp;
                case ep:
                    return this.ep;
                case sp:
                    return this.sp;
                case cp:
                    return this.cp;
            }
        }

        public Collection<Item> collectItems() {
            return List.of(pp.item, gp.item, ep.item, sp.item, cp.item);
        }

        @Override
        public String toString() {
            return "CoinPurse [cp=" + cp + ", ep=" + ep + ", gp=" + gp + ", pp=" + pp + ", sp=" + sp + "]";
        }
    }
}
