package dev.ebullient.pockets;

import java.util.Optional;

import picocli.CommandLine.Option;

public class PocketAttributes {
    @Option(names = { "-w", "--max-weight" }, description = "Maximum weight of this pocket in pounds")
    Optional<Double> max_weight = Optional.empty();

    @Option(names = { "-v", "--max-volume" }, description = "Maximum volume of this pocket in cubic feet")
    Optional<Double> max_volume = Optional.empty();

    @Option(names = { "-p", "--weight" }, description = "Weight of the pocket itself in pounds")
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-m",
            "--magic" }, negatable = true, defaultValue = "false", description = "Marks a magic (extradimensional) pocket")
    boolean magic = false;

    @Option(names = { "-c", "--constraints" }, description = "Additional constraints to remember")
    Optional<String> constraints;

    public PocketAttributes() {
    }

    boolean isComplete() {
        return max_weight.isPresent() && max_volume.isPresent() && weight.isPresent();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "max_weight=" + max_weight +
                ", max_volume=" + max_volume +
                ", weight=" + weight +
                ", magic=" + magic +
                ", constraints=" + constraints +
                '}';
    }
}
