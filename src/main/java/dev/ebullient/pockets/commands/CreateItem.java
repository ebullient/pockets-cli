package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.GIFT;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import java.util.List;

import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.actions.ModificationRequest;
import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.commands.attributes.BulkAttributes;
import dev.ebullient.pockets.commands.attributes.ItemAttributes;
import dev.ebullient.pockets.commands.attributes.PocketAttributes;
import dev.ebullient.pockets.commands.attributes.TransferAttributes;
import dev.ebullient.pockets.commands.attributes.WeightAttributes;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.PocketsFormat;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "create",
        footerHeading = "%nAdditional notes:%n",
        footer = {
                "",
                "- Use the '--pocket' option to create a new pocket:",
                "",
                "    pockets create -p intrepid-heroes \\",
                "      --pocket  \\",
                "      --type=backpack --from=start \\",
                "      \"Sarah's backpack\"",
                "",
                "    This creates a new pocket called \"Sarah's backpack\"",
                "      -p intrepid-heroes : Item is created in this profile",
                "      --type=backpack    : The 'backpack' preset defines weight, etc.",
                "      --from=start       : marks this pocket as coming from starting equipment",
                "    It will have a generated id: sarahs-backpack",
                "",
                "- To create a reference item, leave 'to' and 'from' unset (quantity will be ignored).",
                "",
                "  pockets create -p intrepid-heroes --type=arrow --no-trade --magic \\",
                "    --notes='Talking arrow. Loves Shakespeare. Annoying.' \\",
                "    Arrow of Dedona",
                "",
                "  This creates a new item named \"Arrow of Dedona\"",
                "    -p intrepid-heroes : item is created in this profile",
                "    --type=arrow       : The 'arrow' preset defines weight, etc.",
                "    --no-trade         : indicates the item has no tradable value",
                "    --magic            : indicates a magic item",
                "    --notes='...'      : adds notes describing the item",
                "  It will have a generated id: arrow-of-dedona",
                "",
                "- To create a custom item and add it to a pocket, use 'to' and optionally 'from'",
                "",
                "  pockets create -p intrepid-heroes \\",
                "    --datetime=session-27",
                "    --memo=\"Loot from tomb full of snake-like things\" \\",
                "    --from=loot --to=sarahs-backpack \\",
                "    --value=50gp --weight=.5 \\",
                "    --notes=\"Tiny but very detailed carving found in tomb\" \\",
                "    --id=tiny-jade-crocodile \\",
                "    Carved jade crocodile",
                "",
                "  This creates a new item named \"Carved ivory crocodile\"",
                "    -p intrepid-heroes    : item is created in this profile",
                "    --datetime=session-27 : use a session number for when this change occurred",
                "    --memo='...'          : add a memo describing the change (what/why/where)",
                "    --value=50gp          : item is worth 50gp",
                "    --weight=.5           : item weighs 0.5 lbs (D&D 5e)",
                "    --from=loot           : this item was found",
                "    --to=sarahs-backpack  : add item to Sarah's backpack",
                "    --notes='...'         : describe the item",
                "    --id='...'            : assigns the id of the item",
                "",
                "- Create a special bottomless pocket representing a Shop:",
                "",
                "  pockets -p intrepid-heroes create --pocket --type=shop \\",
                "    --datetime=session-15 \\",
                "    --notes=\"Shop in Sunnyvale. Specializes in building supplies.\" \\",
                "    Little Pig Emporium",
                "",
                "  See 'shop' in --types for details.",
                "",
                "- Create a pocket representing a D&D 5e PC with STR 15:",
                "",
                "  pockets create -p intrepid-heroes --pocket --type=pc \\",
                "    --datetime=session-0 \\",
                "    --max-weight=225 Brunhilde",
                "",
                "  See 'pc' in --types for details.",
                "",
        },
        header = GIFT + NBSP + "Create an item or pocket.",
        description = "%nWhen creating an item:" +
                "%n - Name must be globally unique." +
                "%n - Use a reference item to fill in default values (see --types)" +
                "%n" +
                "%nWhen creating a pocket:" +
                "%n - All custom attributes are valid (item and pocket)" +
                "%n")
public class CreateItem extends BaseCommand {
    @ArgGroup(heading = "%nTransfer Attributes%n", exclusive = false)
    TransferAttributes xferAttributes = new TransferAttributes();

    @ArgGroup(heading = "%nCustom Item or Pocket Attributes%n", exclusive = false)
    ItemAttributes itemAttributes = new ItemAttributes();

    @ArgGroup(heading = "%nCustom Pocket Attributes%n", exclusive = false)
    PocketAttributes pocketAttributes = new PocketAttributes();

    @ArgGroup(heading = "%nEncumbrance: Weight (D&D 5e)%n", exclusive = false)
    WeightAttributes weight = new WeightAttributes();

    @ArgGroup(heading = "%nEncumbrance: Bulk (PF2e)%n", exclusive = false)
    BulkAttributes bulk = new BulkAttributes();

    String name = null;

    @Parameters(index = "0",
            description = "Name of new item", arity = "0..*")
    void setName(List<String> words) {
        name = String.join(" ", words);
    }

    @Override
    public Integer delegateCall() {
        Modification modification = createModification();
        ModificationRequest req = createModificationRequest("Create " + modification.itemDetails.name)
                .add(modification);

        // Make the modification
        ModificationResponse response = modifyPocketRoute.modifyPockets(req);

        response.changes.stream().filter(c -> c.type == PostingType.CREATE)
                .forEach(c -> {
                    Tui.println();
                    if (c.itemType == ItemType.POCKET) {
                        Pocket p = (Pocket) c.created;
                        Tui.create("Created " + PocketsFormat.nameNumberId(p));
                        showPocket(p);
                        showPocketItems(p);
                    } else if (c.itemType == ItemType.ITEM) {
                        Item i = (Item) c.created;
                        Tui.create("Created " + PocketsFormat.nameNumberId(i));
                        showItem(i);
                    }
                });

        return ExitCode.OK;
    }

    private Modification createModification() {
        Profile profile = profileContext.getActiveProfile();
        ProfileConfigData pcd = profile.config;

        Modification mod = new Modification()
                .setType(PostingType.CREATE)
                .setItemType(itemAttributes.isPocket ? ItemType.POCKET : ItemType.ITEM);

        final ItemDetails details = itemAttributes.isPocket
                ? new PocketDetails()
                : new ItemDetails();

        // If a type is specified, apply those defaults
        itemAttributes.applyType(pcd, mod, details);

        // Prompt for missing id & name
        name = itemAttributes.applyName(mod, details, name);

        // Creating a new item, assign/prompt additional attributes
        itemAttributes.apply(pcd, mod, details);
        if (itemAttributes.isPocket && details instanceof PocketDetails) {
            pocketAttributes.apply(pcd, (PocketDetails) details, mod);
        }
        if (pcd.preset == PresetFlavor.dnd5e) {
            weight.apply(details);
        } else if (pcd.preset == PresetFlavor.pf2e) {
            bulk.apply(details);
        }

        // Prompt for to/from/quantity
        xferAttributes.apply(this, profile, mod);
        return mod;
    }
}
