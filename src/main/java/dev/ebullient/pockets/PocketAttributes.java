package dev.ebullient.pockets;

import java.util.Optional;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.PocketReference;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;

public class PocketAttributes {
    @Option(names = { "-w",
            "--max-weight" }, description = "Maximum weight of this pocket in pounds.", showDefaultValue = Visibility.NEVER)
    Optional<Double> max_weight = Optional.empty();

    @Option(names = { "-v",
            "--max-volume" }, description = "Maximum volume of this pocket in cubic feet.", showDefaultValue = Visibility.NEVER)
    Optional<Double> max_volume = Optional.empty();

    @Option(names = { "-p",
            "--weight" }, description = "Weight of the pocket itself in pounds.", showDefaultValue = Visibility.NEVER)
    Optional<Double> weight = Optional.empty();

    @Option(names = { "-m",
            "--magic" }, negatable = true, description = "Marks a magic (extradimensional) pocket.")
    Optional<Boolean> magic = Optional.empty();

    @Option(names = { "-n",
            "--notes" }, description = "Additional notes to remember", showDefaultValue = Visibility.NEVER)
    Optional<String> notes = Optional.empty();

    public PocketAttributes() {
    }

    public void applyWithDefaults(Pocket pocket, PocketReference pRef, PocketTui tui) {
        applyTo(pocket);
        if (tui.interactive()) {
            if ( pRef.isCustom() ) {
                promptForAttributes(pocket, tui);
            } else {
                promptForUpdates(pocket, tui);
            }
        }
    }

    public void applyWithExisting(Pocket pocket, PocketTui tui) {
        applyTo(pocket);
        if (tui.interactive()) {
            promptForUpdates(pocket, tui);
        }
    }

    private void applyTo(Pocket pocket) {
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
        if (notes.isPresent()) {
            pocket.notes = notes.get();
        }
    }

    private void promptForAttributes(Pocket pocket, PocketTui tui) {
        String line = null;
        if (weight.isEmpty()) {
            line = tui.reader().prompt("Weight of this pocket (when empty) in pounds (e.g. 0.25, or 7): ");
            pocket.weight = Mapper.toDouble(line, tui).orElse(null);
        }
        if (magic.isEmpty()) {
            tui.outPrintln("Magic pockets always weigh the same, regardless of their contents.");
            line = tui.reader().prompt("Is this a magic pocket (y/N)? ");
            pocket.extradimensional = Mapper.toBooleanOrDefault(line, false);
        }
        if (max_weight.isEmpty()) {
            line = tui.reader().prompt("Enter the maximum weight of this pocket in pounds (e.g. 1, or 0.5): ");
            pocket.max_weight = Mapper.toDouble(line, tui).orElse(null);
        }
        if (max_volume.isEmpty()) {
            line = tui.reader().prompt("Enter the maximum volume of this pocket in cubic feet (e.g. 2, or 0.75): ");
            pocket.max_volume = Mapper.toDouble(line, tui).orElse(null);
        }
        if (notes.isEmpty()) {
            line = tui.reader().prompt("Enter any additional notes for this pocket (or leave empty): ");
            pocket.notes = line.isEmpty() ? null : line;
        }
    }

    private void promptForUpdates(Pocket pocket, PocketTui tui) {
        String line = null;
        tui.outPrintln("Press enter to keep the previous value.");

        if (weight.isEmpty()) {
            line = tui.reader().prompt("Weight of this pocket (when empty) in pounds [" + pocket.weight + "]: ");
            pocket.weight = Mapper.toDoubleOrDefault(line, pocket.weight, tui);
        }
        if (magic.isEmpty()) {
            tui.outPrintln("Magic pockets always weigh the same, regardless of their contents.");
            line = tui.reader().prompt("Is this a magic pocket [" + (pocket.extradimensional ? "Y" : "N") + "]? ");
            pocket.extradimensional = Mapper.toBooleanOrDefault(line, pocket.extradimensional);
        }

        tui.outPrintln("For the following prompts, use a space to remove the previous value.");
        if (max_weight.isEmpty()) {
            line = tui.reader().prompt("Enter the maximum weight of this pocket in pounds [" + pocket.max_weight + "]: ");
            if (line.length() > 0 && line.isBlank()) {
                pocket.max_weight = null;
            } else {
                pocket.max_weight = Mapper.toDoubleOrDefault(line, pocket.max_weight, tui);
            }
        }
        if (max_volume.isEmpty()) {
            line = tui.reader().prompt("Enter the maximum volume of this pocket in cubic feet [" + pocket.max_volume + "]: ");
            if (line.length() > 0 && line.isBlank()) {
                pocket.max_volume = null;
            } else {
                pocket.max_volume = Mapper.toDoubleOrDefault(line, pocket.max_volume, tui);
            }
        }
        if (notes.isEmpty()) {
            line = tui.reader().prompt("Enter any additional notes for this pocket [" + pocket.notes + "]: ");
            pocket.notes = line.isEmpty() ? null : line;
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "max_weight=" + max_weight +
                ", max_volume=" + max_volume +
                ", weight=" + weight +
                ", magic=" + magic +
                ", notes=" + notes +
                '}';
    }
}
