package dev.ebullient.pockets.commands.attributes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.commands.BaseCommand;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.Profile;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public class TransferAttributes {

    @Option(order = 30, names = { "-q", "--quantity" },
            description = "Quantity of items to add, remove, or transfer. A default value of '1' will be used if the item is being added to a pocket.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Long quantity = null;

    @Option(order = 31, names = { "-f", "--from" },
            description = "Item source. Use one of the following:%n  'loot' for found items,%n  'start' for starting equipment,%n  'given' for items given to you,%n  [id|name] of an existing pocket to remove items from.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String from = null;

    @Option(order = 32, names = { "-t", "--to" },
            description = "Item destination or reason for removal. Use one of the following:%n  'consumed' for consumed or destroyed items,%n  'given' for items given to others,%n  [id|name] of an existing pocket to add items to.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String to = null;

    public void apply(BaseCommand baseCommand, Profile profile, Modification mod) {
        if (Tui.interactive()) {
            from = Tui.promptIfMissing("What is the source of this item (optional; loot, start, given, [pocket id])", from);
            to = Tui.promptIfMissing("Where should this item be placed (optional; consumed, given, [pocket id])", to);

            if (to != null && quantity == null) {
                String value = Tui.promptForValueWithFallback(
                        String.format("How many items should be added to %s? (optional)", to), "1");
                quantity = Long.valueOf(value);
            }
        }

        if (from != null) {
            Pocket fromPocket = baseCommand.selectPocketByLongNameOrId(profile, from);
            mod.setFromPocketId(fromPocket.slug);
        }
        if (to != null) {
            Pocket toPocket = baseCommand.selectPocketByLongNameOrId(profile, to);
            mod.setToPocketId(toPocket.slug);
        }
        mod.setQuantity(quantity);
    }
}
