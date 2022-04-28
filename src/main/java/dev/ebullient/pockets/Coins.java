package dev.ebullient.pockets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Currency;
import dev.ebullient.pockets.db.Currency.CoinPurse;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.TypeConversionException;

@Command(name = "$", aliases = { "coins" }, header = "Do you have change in your pocket?")
public class Coins extends BaseCommand {
    static final Pattern COIN_QUANTITY = Pattern.compile("([0-9]+)([pgesc]p)");
    static final Currency[] ORDER = { Currency.sp, Currency.ep, Currency.gp, Currency.pp };

    enum Operation {
        ADD,
        REMOVE
    }

    @Inject
    Index index;

    @Parameters(index = "0", description = "Name or ID of the target Pocket. Use quotes if there are spaces.")
    String pocketId;

    @Parameters(index = "1", description = "Operation: [a, add, +, r, remove, -]", converter = OperationConverter.class)
    Operation operation;

    @Parameters(index = "2", description = "Space separated list of quantity with units (pp, gp, ep, sp, cp), e.g. '250gp 2sp 5cp'.", arity = "1..*")
    List<String> values;

    @Override
    @Transactional
    public Integer call() throws Exception {
        tui.debug(this.toString());

        Pocket pocket = selectPocketByNameOrId(pocketId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }

        if (tui.interactive()) {
            if (operation == null) {
                String op = tui.reader().prompt("Enter operation (+, -, add, remove): ");
                operation = new OperationConverter().convert(op);
            }
            if (values == null || values.isEmpty()) {
                String v = tui.reader().prompt("Enter space separated list of values, e.g. 1gp 2ep 3sp: ");
                values = List.of(v.split(" "));
            }
        }

        CoinPurse allCoins = new CoinPurse(pocket, tui, index);
        Map<Currency, Integer> quantities = new HashMap<>();
        int deltaCpValue = parseCoinDeltas(quantities);

        if (operation == Operation.ADD) {
            quantities.forEach(allCoins::add);
        } else if (!allCoins.deduct(deltaCpValue)) {
            tui.warnf(
                    "You are trying to remove more coins than you have. You have the equivalent of %s gp, and are trying to remove %s gp.",
                    allCoins.totalCpValue() * Currency.cp.gpEx,
                    deltaCpValue * Currency.cp.gpEx);
            tui.outPrintln("No changes have been saved.");
            return PocketTui.INSUFFICIENT_FUNDS;
        }

        pocket.persistAndFlush(); // save changes to the pocket (and contained items)
        tui.verbose(tui.format().describe(pocket, allCoins));
        return ExitCode.OK;
    }

    /**
     * Calculate the total value (across coin types)
     *
     * @param quantities Remember the requested delta by type
     * @return cumulative cp value
     */
    private int parseCoinDeltas(Map<Currency, Integer> quantities) {
        int deltaCpValue = 0;
        for (String v : values) {
            Matcher m = COIN_QUANTITY.matcher(v);
            if (m.matches()) {
                int quantity = Integer.parseInt(m.group(1)); // amount
                if (quantity <= 0) {
                    tui.warnf("Ignoring %s, no change", v);
                    continue;
                }
                Currency type = Currency.valueOf(m.group(2));
                quantities.merge(type, quantity, Integer::sum);
                deltaCpValue += quantity * type.cpEx;
            } else {
                tui.warnf("Ignoring %s, unknown coin value", v);
            }
        }
        return deltaCpValue;
    }

    @Override
    public String toString() {
        return "Coins [operation=" + operation + ", pocketId=" + pocketId + ", values=" + values + "]";
    }

    static class OperationConverter implements ITypeConverter<Operation> {
        OperationConverter() {
        }

        @Override
        public Operation convert(String value) throws Exception {
            if (value.length() < 1) {
                throw new TypeConversionException(
                        "Invalid value for operation. Accepted values are ['a', 'add', '+', 'r', 'remove', '-'] but was '"
                                + value + "'");
            }
            char c = Character.toLowerCase(value.charAt(0));

            if (c == 'a' || c == '+') {
                return Operation.ADD;
            }
            if (c == 'r' || c == '-') {
                return Operation.REMOVE;
            }
            throw new TypeConversionException(
                    "Invalid value for operation. Accepted values are ['a', 'add', '+', 'r', 'remove', '-'] but was '" + value
                            + "'");
        }
    }
}
