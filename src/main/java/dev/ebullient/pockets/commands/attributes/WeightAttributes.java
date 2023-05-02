package dev.ebullient.pockets.commands.attributes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.PocketDetails;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public class WeightAttributes {

    @Option(order = 40, names = { "--weight" },
            description = "Weight in pounds of:%n  a single item, or%n  a pocket (not including contents)",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Double weight = null;

    @Option(order = 41, names = { "--max-weight" },
            description = "(Pocket only) Maximum capacity (weight) in pounds. This can be a cumulative value if the pocket has multiple compartments.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Double maxWeight = null;

    @Option(order = 42, names = { "--max-volume" },
            description = "(Pocket only) Maximum capacity (volume) in cubic feet. This can be a cumulative value if the pocket has multiple compartments.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Double maxVolume = null;

    public void apply(ItemDetails details) {
        PocketDetails pocketDetails = details instanceof PocketDetails ? (PocketDetails) details : null;
        if (Tui.interactive()) {
            if (weight == null) {
                String value = Tui.promptForValueWithFallback("How much does this item weigh? (optional, in pounds)",
                        details.weight.toString()); // default from ref if present
                weight = Double.valueOf(value);
            }
            details.weight = weight;

            if (pocketDetails != null) {
                if (maxWeight == null) {
                    String value = Tui.promptForValueWithFallback("How much can this item carry? (optional, in pounds)",
                            pocketDetails.maxWeight.toString()); // default from ref if present
                    maxWeight = Double.valueOf(value);
                }
                if (maxVolume == null) {
                    String value = Tui.promptForValueWithFallback(
                            "What is the maximum capacity of this item? (optional volume, cubic feet)",
                            pocketDetails.maxVolume.toString()); // default from ref if present
                    maxVolume = Double.valueOf(value);
                }
                pocketDetails.maxWeight = maxWeight;
                pocketDetails.maxVolume = maxVolume;
            }
        } else {
            details.weight = weight == null ? details.weight : weight;
            if (pocketDetails != null) {
                pocketDetails.maxWeight = maxWeight == null ? pocketDetails.maxWeight : maxWeight;
                pocketDetails.maxVolume = maxVolume == null ? pocketDetails.maxVolume : maxVolume;
            }
        }
    }
}
