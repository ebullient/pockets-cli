
package dev.ebullient.pockets.commands.attributes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.PocketDetails;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public class PocketAttributes {

    @Option(order = 21, names = { "--bottomless" },
            negatable = true,
            description = "This is a bottomless pocket that can always supply or receive items. This pocket might represent a shop or an NPC that is a frequent trading partner. For example, 'loot' and 'given' are built-in bottomless pockets.",
            showDefaultValue = Visibility.NEVER,
            scope = ScopeType.LOCAL)
    public Boolean bottomless = null;

    public void apply(ProfileConfigData pcd, PocketDetails details, Modification mod) {
        if (Tui.interactive()) {
            if (bottomless == null) {
                String value = Tui.promptForValueWithFallback(
                        "Is this a bottomless pocket (a pocket that can always supply or receive items, e.g. an NPC or a Shop)? (optional)",
                        details.bottomless.toString()); // default from ref if present
                bottomless = Transform.toBooleanOrDefault(value, bottomless);
            }
            details.bottomless = bottomless;
        } else {
            details.bottomless = bottomless == null ? details.bottomless : bottomless;
        }
    }
}
