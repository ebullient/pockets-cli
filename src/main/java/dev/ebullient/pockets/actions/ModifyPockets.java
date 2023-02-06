package dev.ebullient.pockets.actions;

import static dev.ebullient.pockets.io.PocketTui.CONFLICT;
import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;

import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ExitCode;

@Dependent
public class ModifyPockets {
    @Inject
    ProfileContext context;

    @Transactional
    public ModificationResponse modifyPockets(ModificationRequest req) {
        ModificationResponse resp = new ModificationResponse(req)
                .setProfile(context.getActiveProfileName());

        for (Modification mod : req.changes) {
            switch (mod.type) {
                case CREATE:
                    handleCreateModification(mod);
                    break;
                case MOVE:
                    handleMoveModification(mod);
                    break;
                case ADD:
                    handleAddModification(mod);
                    break;
                case REMOVE:
                    handleRemoveModification(mod);
                    break;
            }
            resp.post(mod.result);
        }
        Tui.debugf("Finished (%s, %s): %s", resp.datetime, resp.memo, resp.changes);

        Profile profile = context.getActiveProfile();
        profile.addJournal(resp.toJournal());
        profile.persistAndFlush();
        return resp;
    }

    private void handleCreateModification(Modification mod) {
        Profile profile = context.getActiveProfile();
        try {
            switch (mod.itemType) {
                case CURRENCY: {
                    throw new InvalidPocketState(true, "Define new kinds of currency in profile settings");
                }
                case POCKET: {
                    Pocket p = createPocket(mod, mod.itemId, mod.createRef, (PocketDetails) mod.itemDetails);
                    removeItemIfBottomlessPocket(mod, p.slug, 1L);
                    if (mod.toPocketId != null) {
                        addPocketToPocket(mod, mod.toPocketId, p);
                    }
                    break;
                }
                default: {
                    Item i = findOrCreateItem(mod, mod.itemId, mod.createRef, mod.itemDetails);
                    if (mod.toPocketId != null) {
                        long amount = mod.quantity == null ? 1 : mod.quantity;
                        removeItemIfBottomlessPocket(mod, i.slug, amount);

                        addItemToPocket(mod, mod.toPocketId, i, amount);
                    }
                    break;
                }
            }
        } catch (EntityExistsException ex) {
            throw new InvalidPocketState(PocketTui.CONFLICT, "%s identifier %s already exists for profile %s.",
                    mod.itemType == ItemType.POCKET ? "Pocket" : "Item", mod.itemId, profile.id);
        }
    }

    private void handleMoveModification(Modification mod) {
        if (mod.itemId == null || mod.fromPocketId == null || mod.toPocketId == null) {
            throw new InvalidPocketState(true,
                    "Must specify the item (%s), the pocket the item should be moved from (%s), and the pocket the item should be moved to (%s)",
                    mod.itemId, mod.fromPocketId, mod.toPocketId);
        }
        Profile profile = context.getActiveProfile();
        ProfileConfigData pcd = profile.config;
        switch (mod.itemType) {
            case CURRENCY: {
                if (mod.quantity == null && pcd.isCurrencyString(mod.itemId)) {
                    Pocket toPocket = Pocket.findByNaturalId(profile, mod.toPocketId);
                    Pocket fromPocket = Pocket.findByNaturalId(profile, mod.fromPocketId);
                    Map<String, Long> changes = pcd.parseCurrencyChanges(mod.itemId);
                    changes.forEach((k, v) -> {
                        long quantity = removeCurrencyFromPocket(mod, fromPocket, k, v);
                        addCurrencyToPocket(mod, toPocket, k, quantity);
                    });
                } else {
                    long quantity = removeCurrencyFromPocket(mod, mod.fromPocketId, mod.itemId, mod.quantity);
                    addCurrencyToPocket(mod, mod.toPocketId, mod.itemId, quantity);
                }
                break;
            }
            case POCKET: {
                Pocket p = Pocket.findByNaturalId(profile, mod.itemId);
                removePocket(mod, mod.fromPocketId, p);
                addPocketToPocket(mod, mod.toPocketId, p);
                break;
            }
            default: {
                long quantity = mod.quantity == null ? 1 : mod.quantity;
                Item i = Item.findByNaturalId(profile, mod.itemId);
                quantity = removeItemFromPocket(mod, mod.fromPocketId, i, quantity);
                addItemToPocket(mod, mod.toPocketId, i, quantity);
                break;
            }
        }
    }

    private void handleAddModification(Modification mod) {
        if (mod.itemId == null || mod.toPocketId == null) {
            throw new InvalidPocketState(true,
                    "Must specify the item (%s) and the pocket the item should be added to (%s)",
                    mod.itemId, mod.toPocketId);
        }
        Profile profile = context.getActiveProfile();
        ProfileConfigData pcd = profile.config;
        switch (mod.itemType) {
            case CURRENCY: {
                if (mod.quantity == null && pcd.isCurrencyString(mod.itemId)) {
                    Pocket toPocket = Pocket.findByNaturalId(profile, mod.toPocketId);
                    Map<String, Long> changes = pcd.parseCurrencyChanges(mod.itemId);
                    changes.forEach((k, v) -> addCurrencyToPocket(mod, toPocket, k, v));
                } else {
                    addCurrencyToPocket(mod, mod.toPocketId, mod.itemId, mod.quantity);
                }
                break;
            }
            case POCKET: {
                Pocket p = Pocket.findByNaturalId(profile, mod.itemId);
                addPocketToPocket(mod, mod.toPocketId, p);
                break;
            }
            default: {
                Item item = findOrCreateItem(mod, mod.itemId, mod.createRef == null
                        ? mod.itemId
                        : mod.createRef, mod.itemDetails);
                addItemToPocket(mod, mod.toPocketId, item, mod.quantity);
                break;
            }
        }
    }

    private void handleRemoveModification(Modification mod) {
        if (mod.itemType == ItemType.ITEM && (mod.itemId == null || mod.fromPocketId == null)) {
            throw new InvalidPocketState(true,
                    "Must specify the item (%s) and the pocket the item should be removed from (%s)",
                    mod.itemId, mod.fromPocketId);
        } else if (mod.itemType == ItemType.POCKET && mod.itemId == null) {
            throw new InvalidPocketState(true, "Must specify the pocket to remove");
        }

        Profile profile = context.getActiveProfile();
        ProfileConfigData pcd = profile.config;
        switch (mod.itemType) {
            case CURRENCY: {
                if (mod.quantity == null && pcd.isCurrencyString(mod.itemId)) {
                    Pocket fromPocket = Pocket.findByNaturalId(profile, mod.fromPocketId);
                    Map<String, Long> changes = pcd.parseCurrencyChanges(mod.itemId);
                    changes.forEach((k, v) -> removeCurrencyFromPocket(mod, fromPocket, k, v));
                } else {
                    removeCurrencyFromPocket(mod, mod.fromPocketId, mod.itemId, mod.quantity);
                }
                break;
            }
            case POCKET: {
                Pocket p = Pocket.findByNaturalId(profile, mod.itemId);
                removePocket(mod, mod.fromPocketId, p);
                break;
            }
            default: {
                Item i = Item.findByNaturalId(profile, mod.itemId);
                removeItemFromPocket(mod, mod.fromPocketId, i, mod.quantity); // TODO: default 1 or all?
                break;
            }
        }
    }

    private void addPocketToPocket(Modification mod, String targetId, Pocket nested) {
        Profile profile = context.getActiveProfile();
        if (nested.parentPocket != null) {
            if (targetId.equals(nested.parentPocket.slug)) {
                return;
            }
            removePocketFromParent(mod, nested);
        }

        Pocket parent = Pocket.findByNaturalId(profile, targetId);
        parent.addPocket(nested);
        parent.persist(); // ✨
        mod.record(PostingType.ADD)
                .setPocketId(parent.slug)
                .setItemType(ItemType.POCKET).setItemId(nested.slug).setQuantity(1L);
    }

    private void removePocketFromParent(Modification mod, Pocket nested) {
        Pocket oldParent = nested.parentPocket;
        if (oldParent != null) {
            oldParent.removePocket(nested);
            oldParent.persist(); // ✨
            mod.record(PostingType.REMOVE)
                    .setPocketId(oldParent.slug)
                    .setItemType(ItemType.POCKET).setItemId(nested.slug).setQuantity(1L);
        }
    }

    private void removeItemIfBottomlessPocket(Modification mod, String itemId, long quantity) {
        Profile profile = context.getActiveProfile();
        if (mod.fromPocketId != null) {
            Pocket from = Pocket.findByNaturalId(profile, mod.fromPocketId);
            if (from.isBottomless()) {
                mod.record(PostingType.REMOVE)
                        .setPocketId(from.slug)
                        .setItemType(mod.itemType).setItemId(itemId).setQuantity(quantity);
            } else {
                throw new InvalidPocketState(ExitCode.USAGE,
                        "Pocket (%s) does not contain item (%s) (the item does not yet exist).", mod.fromPocketId, mod.itemId);
            }
        }
    }

    private void removePocket(Modification mod, String fromId, Pocket pocket) {
        Profile profile = context.getActiveProfile();

        if (fromId != null) {
            if (pocket.parentPocket == null) {
                throw new InvalidPocketState(PocketTui.BAD_DATA, "Pocket %s (%s) is not nested in another pocket.",
                        pocket.name, pocket.slug);
            } else if (fromId.equals(pocket.parentPocket.slug)) {
                removePocketFromParent(mod, pocket);
            } else {
                throw new InvalidPocketState(PocketTui.BAD_DATA, "Pocket %s (%s) is not nested inside pocket %s.",
                        pocket.name, pocket.slug, fromId);
            }
        } else {
            profile.removePocket(pocket); // 💥
            profile.persist(); // cascade
            mod.record(PostingType.REMOVE)
                    .setItemType(ItemType.POCKET).setItemId(pocket.slug).setQuantity(1L);
        }
    }

    private void addItemToPocket(Modification mod, String targetId, Item item, long quantity) {
        Profile profile = context.getActiveProfile();
        Pocket target = profile.getPocket(targetId);

        target.addItem(item, quantity);
        target.persist(); // ✨
        mod.record(PostingType.ADD)
                .setPocketId(target.slug)
                .setItemType(ItemType.ITEM).setItemId(item.slug).setQuantity(quantity);
    }

    private long removeItemFromPocket(Modification mod, String targetId, Item item, long quantity) {
        Profile profile = context.getActiveProfile();
        Pocket target = profile.getPocket(targetId);

        long removed = target.removeItem(item, quantity);
        target.persist(); // ✨
        mod.record(PostingType.REMOVE)
                .setPocketId(target.slug)
                .setItemType(ItemType.ITEM).setItemId(item.slug).setQuantity(removed);
        return removed;
    }

    private long addCurrencyToPocket(Modification mod, String toPocketId, String c, Long quantity) {
        Profile profile = context.getActiveProfile();
        Pocket target = profile.getPocket(toPocketId);

        return addCurrencyToPocket(mod, target, c, quantity);
    }

    private long addCurrencyToPocket(Modification mod, Pocket toPocket, String c, Long quantity) {
        long total = toPocket.addCurrency(c, quantity);
        toPocket.persist(); // ✨

        mod.record(PostingType.ADD)
                .setPocketId(toPocket.slug)
                .setItemType(ItemType.CURRENCY).setItemId(c).setQuantity(quantity);
        return total;
    }

    private long removeCurrencyFromPocket(Modification mod, String fromPocketId, String c, Long quantity) {
        Profile profile = context.getActiveProfile();
        Pocket target = profile.getPocket(fromPocketId);
        if (target.isBottomless()) {
        }
        return removeCurrencyFromPocket(mod, target, c, quantity);
    }

    private long removeCurrencyFromPocket(Modification mod, Pocket fromPocket, String c, Long quantity) {
        long removed = fromPocket.withdraw(c, quantity);
        fromPocket.persist(); // ✨

        mod.record(PostingType.REMOVE)
                .setPocketId(fromPocket.slug)
                .setItemType(ItemType.CURRENCY).setItemId(c).setQuantity(removed);
        return removed;
    }

    private Pocket createPocket(Modification mod, String pocketId, String refId, PocketDetails details) {
        Profile profile = context.getActiveProfile();

        Pocket pocket = new Pocket();
        pocket.setPocketDetails(profile, pocketId,
                (details == null ? new PocketDetails() : details).setRefId(refId));

        if (profile.getPocket(pocket.slug) != null) {
            throw new InvalidPocketState(CONFLICT, "Profile %s already contains a pocket with id %s",
                    profile.slug, pocket.slug);
        }
        profile.addPocket(pocket); // Add pocket to profile (cascade)
        profile.persist(); // ✨

        mod.record(PostingType.CREATE)
                .setItemType(ItemType.POCKET).setItemId(pocket.slug).setCreated(pocket);
        return pocket;
    }

    private Item findOrCreateItem(Modification mod, String itemId, String refId, ItemDetails details) {
        Profile profile = context.getActiveProfile();
        Item item = profile.getItem(itemId);
        return item == null ? createItem(mod, itemId, refId, details) : item;
    }

    private Item createItem(Modification mod, String itemId, String refId, ItemDetails details) {
        Profile profile = context.getActiveProfile();
        Item item = new Item();
        item.setItemDetails(profile, itemId,
                (details == null ? new ItemDetails() : details).setRefId(refId));

        if (profile.getItem(item.slug) != null) {
            throw new InvalidPocketState(CONFLICT, "Profile %s already contains an item with id %s",
                    profile.slug, item.slug);
        }
        profile.addItem(item); // Add item through the profile (cascade)
        profile.persist(); // ✨

        mod.record(PostingType.CREATE)
                .setItemType(ItemType.ITEM).setItemId(item.slug).setCreated(item);
        return item;
    }
}
