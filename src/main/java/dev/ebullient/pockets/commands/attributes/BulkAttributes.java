package dev.ebullient.pockets.commands.attributes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.PocketDetails;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public class BulkAttributes {
    @Option(order = 50, names = { "-b", "--bulk" },
            description = "Bulk of:%n  a single item, or%n  a pocket (not including contents)",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String bulk = null;

    @Option(order = 51, names = { "--max-bulk" },
            description = "(Pocket only) Maximum Bulk that can fit in this pocket.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String maxBulk = null;

    public void apply(ItemDetails details) {
        PocketDetails pocketDetails = details instanceof PocketDetails ? (PocketDetails) details : null;
        if (Tui.interactive()) {
            if (bulk == null) {
                bulk = Tui.promptForValueWithFallback("How much bulk can this item carry? (optional)",
                        details.bulk); // default from ref if present
            }
            details.bulk = bulk;

            if (pocketDetails != null) {
                if (maxBulk == null) {
                    maxBulk = Tui.promptForValueWithFallback("What is the maximum bulk this pocket can hold? (optional)",
                            pocketDetails.maxBulk); // default from ref if present
                }
                pocketDetails.maxBulk = maxBulk;
            }
        } else {
            details.bulk = bulk == null ? details.bulk : bulk;
            if (pocketDetails != null) {
                pocketDetails.maxBulk = maxBulk == null ? pocketDetails.maxBulk : maxBulk;
            }
        }
    }
}
