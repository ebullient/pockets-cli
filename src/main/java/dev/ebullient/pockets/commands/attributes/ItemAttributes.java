package dev.ebullient.pockets.commands.attributes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.io.InvalidPocketState;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public class ItemAttributes {

    @Option(order = 10, names = { "--type" },
            description = "Item or pocket reference type (see --types)",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String typeId = null;

    @Option(order = 8, names = { "--pocket" },
            description = "Create a pocket instead of an item",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public boolean isPocket = false;

    @Option(order = 9, names = { "--id" },
            description = "Item id.%n  The default id for new items is derived from its name.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String id;

    @Option(order = 11, names = { "--magic" },
            negatable = true,
            description = "Is this a magical item?%n  Extradimensional pockets have special behavior, and magical items can be difficult to trade.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Boolean magic = null;

    @Option(order = 12, names = { "--trade" },
            negatable = true,
            description = "Is this a tradable item?%n  Tradable items are included in the cumulative value of your pockets.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Boolean tradable = null;

    @Option(order = 13, names = { "--value" },
            description = "The value of one item. Include appropriate units, like pp, gp, sp, or cp.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String value = null;

    @Option(order = 14, names = { "--notes" },
            description = "Additional notes about this item.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public String notes = null;

    public void applyType(ProfileConfigData pcd, Modification mod, ItemDetails details) {
        if (Tui.interactive() && typeId == null) {
            typeId = Tui.promptIfMissing("Id of reference type (see --types)", typeId);
        }
        if (typeId != null) {
            mod.setCreateRef(typeId);
            // set defaults in details ahead of prompts
            ItemRef iRef = pcd.itemRef(typeId);
            if (pcd.hasPocketRef(typeId)) {
                pcd.pocketRef(typeId).applyDefaults(details);
            } else if (iRef != null) {
                iRef.applyDefaults(details);
            } else {
                throw new InvalidPocketState(ExitCode.USAGE, "Unknown reference type %s", typeId);
            }
        }
    }

    public String applyName(Modification mod, ItemDetails details, String name) {
        if (Tui.interactive()) {
            if (name == null) {
                name = Tui.promptForValueWithFallback("Specify the name of this item",
                        details.name); // default from ref if present
            }
            if (id == null) {
                id = Tui.promptForValueWithFallback("Specify the item id",
                        Transform.slugify(name)); // default is slugified name
            }
        }
        mod.setItemId(id);
        details.name = name == null ? details.name : name;
        return details.name;
    }

    public void apply(ProfileConfigData pcd, Modification mod, ItemDetails details) {
        if (Tui.interactive()) {
            if (value == null) {
                value = Tui.promptForValueWithFallback("Specify a value (worth) for this item (optional; 1gp, 2sp, etc.)",
                        pcd.valueToString(details.baseUnitValue)); // default from ref if present
            }
            details.baseUnitValue = pcd.toBaseValue(value);

            if (notes == null) {
                notes = Tui.promptForValueWithFallback("Any notes for this item? (optional)",
                        details.notes); // default from ref if present
            }
            details.notes = notes;

            if (tradable == null) {
                String value = Tui.promptForValueWithFallback("Is this item tradable? (optional)",
                        details.tradable.toString()); // default from ref if present
                tradable = Transform.toBooleanOrDefault(value, details.tradable != null && details.tradable);
            }
            details.tradable = tradable;

            if (magic == null) {
                String value = Tui.promptForValueWithFallback("Is this item magical? (optional)",
                        details.magical.toString()); // default from ref if present
                magic = Transform.toBooleanOrDefault(value, details.magical != null && details.magical);
            }
            details.magical = magic;
        } else {
            details.baseUnitValue = value == null ? details.baseUnitValue : pcd.toBaseValue(value);
            details.notes = notes == null ? details.notes : notes;
            details.tradable = tradable == null ? details.tradable : tradable;
            details.magical = magic == null ? details.magical : magic;
        }
        mod.setItemDetails(details);
    }
}
