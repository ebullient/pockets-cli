package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.GIFT;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import java.util.List;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.actions.ModificationRequest;
import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.commands.attributes.TransferAttributes;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "add",
        header = GIFT + NBSP + "Add items to a pocket",
        footerHeading = "%nAdditional notes:%n",
        footer = {
                "",
                "- Add currency to an existing pocket:",
                "",
                "  pockets add -p intrepid-heroes \\",
                "    --datetime=session-5",
                "    --memo=\"Found stuffed under the mattress in the old inn\"",
                "    --from=loot --to=backpack \\",
                "      250gp 2sp 5cp",
                "",
                "  This adds 250gp 2sp 5cp to a pocket with id 'backpack'",
                "    -p intrepid-heroes    : item is created in this profile",
                "    --datetime=...        : describe when this change occurred",
                "    --memo=\"...\"        : describe the change (what/why/where)",
                "    --from=loot           : currency was found",
                "    --to=backpack         : add item to backpack",
                "",
        },
        description = "%nWhen adding a known item:" +
                "%n - Use the item's number, name, or id (see list --items)" +
                "%n - Directly specify currency: 250gp 2sp 5cp" +
                "%n")
public class AddItem extends BaseCommand {
    @ArgGroup(heading = "%nTransfer Attributes%n", exclusive = false)
    TransferAttributes xferAttributes = new TransferAttributes();

    String nameOrId = null;

    @Parameters(index = "0",
            description = "Known item or currency value", arity = "0..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    public Integer delegateCall() {
        Profile profile = profileContext.getActiveProfile();
        ProfileConfigData pcd = profile.config;

        nameOrId = Tui.promptIfMissing("Specify the number, name, or id of an existing item, or a currency value", nameOrId);
        if (Transform.isBlank(nameOrId)) {
            throw new InvalidPocketState(ExitCode.USAGE, "Must specify the number, name, or id of an existing item (%s).",
                    nameOrId);
        }

        final ModificationRequest req;
        Modification mod = new Modification().setType(PostingType.ADD);

        // Does the name match a currency string?
        if (pcd.isCurrencyString(nameOrId)) {
            mod.setItemType(ItemType.CURRENCY);
            req = createModificationRequest("Add currency");
        } else {
            Item item = selectItemByLongNameOrId(profile, nameOrId);
            mod.setItemId(item.slug);
            xferAttributes.apply(this, profile, mod);
            req = createModificationRequest(String.format("Add %s to %s", mod.itemId, mod.toPocketId));
        }

        // Make the modification
        ModificationResponse response = modifyPocketRoute.modifyPockets(req.add(mod));

        //response.changes.stream().filter(c -> c.type == PostingType.CREATE)
        //        .forEach(c -> {
        //            Tui.println();
        //            if (c.itemType == ItemType.POCKET) {
        //                Tui.create("Created " + Formatter.describe(pcd, (Pocket) c.created));
        //                Tui.println(Formatter.describePocketContents(pcd, (Pocket) c.created));
        //            } else if (c.itemType == ItemType.ITEM) {
        //                Tui.create("Created " + Formatter.describe(pcd, (Item) c.created));
        //                Tui.println(Formatter.describeItemPockets(pcd, (Item) c.created));
        //            }
        //        });

        return ExitCode.OK;
    }
}
