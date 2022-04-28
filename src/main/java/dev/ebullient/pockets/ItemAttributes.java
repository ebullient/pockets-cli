package dev.ebullient.pockets;

import java.util.Optional;

import dev.ebullient.pockets.db.Currency;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class ItemAttributes {
    @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add.")
    Optional<Integer> quantity = Optional.empty();

    @Option(names = {
            "--trade" }, negatable = true, showDefaultValue = Visibility.NEVER, description = "Is this a tradable item?%n  tradable items are included in the cumulative value (gp) of your pocket.")
    Optional<Boolean> tradable = Optional.empty();

    @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds.")
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-v",
            "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp).")
    Optional<String> value = Optional.empty();

    public void applyWithDefaults(Item item, PocketTui tui) {
        if (tui.interactive()) {
            applyTo(item, tui);
            promptForAttributes(item, tui);
        } else {
            item.quantity = quantity.orElse(1);
            item.tradable = tradable.orElse(true);
            item.weight = weight.orElse(null);
            item.cpValue = value.isPresent()
                    ? Currency.cpValue(value.get(), tui).orElse(null)
                    : null;
        }
    }

    public void applyWithExisting(Item item, PocketTui tui) {
        applyTo(item, tui);
        if (tui.interactive()) {
            promptForUpdates(item, tui);
        }
    }

    private void applyTo(Item item, PocketTui tui) {
        if (quantity.isPresent()) {
            item.quantity = quantity.get();
        }
        if (tradable.isPresent()) {
            item.tradable = tradable.get();
        }
        if (value.isPresent()) {
            item.cpValue = Currency.cpValueOrDefault(value.get(), item.cpValue, tui);
        }
        if (weight.isPresent()) {
            item.weight = weight.get();
        }
    }

    void promptForAttributes(Item item, PocketTui tui) {
        String line = null;
        if (quantity.isEmpty()) {
            line = tui.reader().prompt("How many (" + 1 + "): ");
            item.quantity = (int) Mapper.toLongOrDefault(line, 1, tui);
        }
        if (tradable.isEmpty()) {
            line = tui.reader().prompt("Is this item tradable? (Y/n): ");
            item.tradable = Mapper.toBooleanOrDefault(line, true);
        }
        if (weight.isEmpty()) {
            line = tui.reader().prompt("Enter the weight of this item in pounds (Enter to skip): ");
            if (line.length() > 0 && line.isBlank()) {
                item.weight = null;
            } else {
                item.weight = Mapper.toDoubleOrDefault(line, null, tui);
            }
        }
        if (value.isEmpty()) {
            line = tui.reader().prompt("Enter the value of a single item (1gp, 2sp, etc.): ");
            if (line.length() > 0 && line.isBlank()) {
                item.cpValue = null;
            } else {
                item.cpValue = Currency.cpValueOrDefault(line, null);
            }
        }
    }

    void promptForUpdates(Item item, PocketTui tui) {
        String line = null;
        tui.outPrintln("Press enter to keep the previous value.");

        if (quantity.isEmpty()) {
            line = tui.reader().prompt("How many (" + item.quantity + "): ");
            item.quantity = (int) Mapper.toLongOrDefault(line, item.quantity, tui);
        }
        if (tradable.isEmpty()) {
            String previous = item.tradable ? "Y" : "N";
            line = tui.reader().prompt("Is this item tradable (" + previous + "): ");
            item.tradable = Mapper.toBooleanOrDefault(line, item.tradable);
        }

        tui.outPrintln("For the following prompts, use a space to remove the previous value.");
        if (value.isEmpty()) {
            line = tui.reader().prompt("Enter the value of a single item (" + item.cpValue + "gp): ");
            if (line.length() > 0 && line.isBlank()) {
                item.cpValue = null;
            } else {
                item.cpValue = Currency.cpValueOrDefault(line, item.cpValue);
            }
        }
        if (weight.isEmpty()) {
            line = tui.reader().prompt("Enter the weight of this item in pounds (" + item.weight + "): ");
            if (line.length() > 0 && line.isBlank()) {
                item.weight = null;
            } else {
                item.weight = Mapper.toDoubleOrDefault(line, item.weight, tui);
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "quantity=" + quantity +
                ", weight=" + weight +
                ", value=" + value +
                ", tradable=" + tradable +
                '}';
    }
}
