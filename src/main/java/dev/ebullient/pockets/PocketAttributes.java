package dev.ebullient.pockets;

import java.util.Optional;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class PocketAttributes {
    @Option(names = { "-w",
            "--max-weight" }, description = "Maximum weight of this pocket in pounds", showDefaultValue = Visibility.NEVER)
    Optional<Double> max_weight = Optional.empty();

    @Option(names = { "-v",
            "--max-volume" }, description = "Maximum volume of this pocket in cubic feet", showDefaultValue = Visibility.NEVER)
    Optional<Double> max_volume = Optional.empty();

    @Option(names = { "-p",
            "--weight" }, description = "Weight of the pocket itself in pounds", showDefaultValue = Visibility.NEVER)
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-m",
            "--magic" }, negatable = true, description = "Marks a magic (extradimensional) pocket")
    Optional<Boolean> magic = Optional.empty();

    @Option(names = { "-c",
            "--constraints" }, description = "Additional constraints to remember", showDefaultValue = Visibility.NEVER)
    Optional<String> constraints;

    public PocketAttributes() {
    }

    public void applyTo(Pocket pocket) {
        if (max_weight.isPresent()) {
            pocket.max_weight = max_weight.get();
        }
        if (max_volume.isPresent()) {
            pocket.max_volume = max_volume.get();
        }
        if (weight.isPresent()) {
            pocket.weight = weight.get();
        }
        if (magic.isPresent()) {
            pocket.extradimensional = magic.get();
        }
    }

    public boolean isComplete() {
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
