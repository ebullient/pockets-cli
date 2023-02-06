package dev.ebullient.pockets.config;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import picocli.CommandLine.ExitCode;

public class ItemDetailResolver<T extends ItemDetails> {

    ItemType type;
    String slugId;
    String name;
    T details;

    protected ItemDetailResolver(ItemType t, String id, String name, T itemDetails) {
        this.type = t;
        this.slugId = id;
        this.details = itemDetails;
        this.name = name == null ? itemDetails.name : name;

        if (slugId == null && name == null && itemDetails.refId == null) {
            throw new InvalidPocketState(
                    ExitCode.USAGE,
                    "Not enough information to create an item. Must specify a name (%s), id (%s), or item reference(%s)",
                    name, slugId, itemDetails.refId);
        }
    }

    private ItemRef getReference(ProfileConfigData context, ItemType type, String refId) {
        if (refId == null) {
            return null;
        }
        if (type == ItemType.POCKET) {
            return context.pocketRef(refId);
        }
        if (type == ItemType.CURRENCY) {
            return context.currencyRef(refId);
        }
        return context.itemRef(refId);
    }

    private <U extends ItemRef> void applyDefaults(ProfileConfigData context) {
        // find item reference (reference may set default name, etc)
        String refId = details.refId == null
                ? (slugId == null ? Transform.slugify(name) : slugId)
                : details.refId;

        ItemRef ref = getReference(context, type, refId);
        if (ref == null) {
            Types.applyGenericDefaults(type, details);
        } else {
            ref.applyDefaults(details);
        }
        name = name == null ? details.name : name;
        details.name = null; // remove input parameter

        slugId = slugId == null ? Transform.slugify(name) : slugId;
    }

    public static void unpack(Profile profile, Item item) {
        if (item.itemDetails == null) {
            item.itemDetails = new ItemDetails();
        }
        ItemDetailResolver<ItemDetails> resolver = new ItemDetailResolver<>(ItemType.ITEM, item.slug, item.name,
                item.itemDetails);
        resolver.applyDefaults(profile.config);
        item.slug = resolver.slugId;
        item.name = resolver.name;
        assert (item.slug != null);
    }

    public static void unpack(Profile profile, Pocket pocket) {
        if (pocket.pocketDetails == null) {
            pocket.pocketDetails = new PocketDetails();
        }
        ItemDetailResolver<PocketDetails> resolver = new ItemDetailResolver<>(ItemType.POCKET, pocket.slug, pocket.name,
                pocket.pocketDetails);
        resolver.applyDefaults(profile.config);
        pocket.slug = resolver.slugId;
        pocket.name = resolver.name;
        pocket.emoji = pocket.pocketDetails.emoji;
        pocket.pocketDetails.emoji = null; // clear duplicate / input
        assert (pocket.slug != null);
    }
}
