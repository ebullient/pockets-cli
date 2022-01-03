package dev.ebullient.pockets;

import java.util.Optional;

import picocli.CommandLine.Option;

public class PocketItemAttributes {
    @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add", defaultValue = "1")
    int quantity = 1;

    @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds")
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-v",
            "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp)")
    Optional<String> value = Optional.empty();

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "quantity=" + quantity +
                ", weight=" + weight +
                ", value=" + value +
                '}';
    }
}
