package dev.ebullient.pockets;

import java.util.Optional;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class ItemAttributes {
    @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add")
    Optional<Integer> quantity = Optional.empty();

    @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds")
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-v",
            "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp)")
    Optional<String> value = Optional.empty();

    @Option(names = {
            "--trade" }, negatable = true, showDefaultValue = Visibility.NEVER, description = "Is this a tradable item?%n  tradable items are included in the cumulative value (gp) of your pocket.")
    Optional<Boolean> tradable = Optional.empty();

    public void applyTo(Item item, PocketTui tui) {
        if (value.isPresent()) {
            item.gpValue = Mapper.Currency.gpValueOrDefault(value.get(), item.gpValue, tui);
        }
        if (weight.isPresent()) {
            item.weight = weight.get();
        }
        if (quantity.isPresent()) {
            item.quantity = quantity.get();
        }
        if (tradable.isPresent()) {
            item.tradable = tradable.get();
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
