package dev.ebullient.pockets;

import java.util.Optional;

import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class PocketItemAttributes {
    @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add", defaultValue = "1")
    int quantity = 1;

    @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds")
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-v",
            "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp)")
    Optional<String> value = Optional.empty();

    @Option(names = {
            "--trade" }, negatable = true, showDefaultValue = Visibility.NEVER, description = "Is this a tradable item?%n  tradable items are included in the cumulative value (gp) of your pocket.")
    Optional<Boolean> tradable = Optional.empty();

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
