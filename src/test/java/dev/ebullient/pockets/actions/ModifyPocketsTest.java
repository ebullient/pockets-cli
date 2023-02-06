package dev.ebullient.pockets.actions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.Util;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.Journal;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.Pocket.SpecialPocket;
import dev.ebullient.pockets.db.PocketItem;
import dev.ebullient.pockets.db.Posting;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ModifyPocketsTest {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    ProfileContext profileContext;

    @Test
    @TestTransaction
    public void testCreateItem() {
        profileContext.setActiveProfile("default");

        ModificationRequest req = new ModificationRequest("New arrows", "1499-03-24");

        req.add(new Modification()
                .create(Posting.ItemType.ITEM, null, "arrow")
                .setFromPocketId(SpecialPocket.start.getId()));

        modifyPockets.modifyPockets(req);

        // This should fail -- duplicate items
        Assertions.assertThrows(InvalidPocketState.class, () -> modifyPockets.modifyPockets(req));

        Profile profile = profileContext.getActiveProfile();

        Item i = Item.findByNaturalId(profile, "arrow");
        assertThat(i).isNotNull();
        assertThat(i.slug).isEqualTo("arrow");
        assertThat(i.itemDetails.tradable).isEqualTo(true);
        assertThat(i.itemDetails.baseUnitValue).isEqualTo(5);

        assertThat(profile.getJournals()).hasSize(1);
        Journal j1 = profile.getJournals().get(0);
        assertThat(j1.datetime).isEqualTo(req.datetime);
        assertThat(j1.changes).hasSize(1);

        Util.checkCreatePosting(j1.changes.get(0), ItemType.ITEM, "arrow", i);
    }

    @Test
    @TestTransaction
    public void testCreateAddItemToPocket() {
        profileContext.setActiveProfile("default");

        ModificationRequest req = new ModificationRequest("New arrows", "1499-03-24");

        req.add(new Modification()
                .create(Posting.ItemType.POCKET, "backpack-of-doom", "backpack")
                .setFromPocketId(SpecialPocket.start.getId()));

        req.add(new Modification()
                .create(Posting.ItemType.ITEM, null, "arrow")
                .setFromPocketId(SpecialPocket.start.getId())
                .setToPocketId("backpack-of-doom"));

        modifyPockets.modifyPockets(req);

        Profile profile = profileContext.getActiveProfile();

        Pocket p = Pocket.findByNaturalId(profile, "backpack-of-doom");
        Item i = Item.findByNaturalId(profile, "arrow");

        Util.checkPocketItemLinks(p, i, 1);

        assertThat(profile.getJournals()).hasSize(1);

        Journal j1 = profile.getJournals().get(0);
        assertThat(j1.datetime).isEqualTo(req.datetime);

        List<Posting> postings = j1.changes;
        assertThat(postings).hasSize(5);

        // A create step...
        Util.checkCreatePosting(postings.get(0), Posting.ItemType.POCKET, "backpack-of-doom", p);
        Util.checkPosting(postings.get(1), Posting.PostingType.REMOVE, Posting.ItemType.POCKET, "p:start", "backpack-of-doom",
                1L);

        // A create step...
        Util.checkCreatePosting(postings.get(2), Posting.ItemType.ITEM, "arrow", i);
        Util.checkPosting(postings.get(3), Posting.PostingType.REMOVE, Posting.ItemType.ITEM, "p:start", "arrow", 1L);

        // Then the add step
        Util.checkPosting(postings.get(4), Posting.PostingType.ADD, Posting.ItemType.ITEM, "backpack-of-doom", "arrow", 1L);
    }

    @Test
    @TestTransaction
    public void testMoveBetweenPockets() {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        ModificationRequest req = new ModificationRequest("Making things", "1499-03-24");

        // Create backpack 1
        req.add(new Modification()
                .create(Posting.ItemType.POCKET, "backpack-1", "backpack")
                .setFromPocketId(SpecialPocket.start.getId()));

        // Create item and add to backpack 1
        req.add(new Modification()
                .create(Posting.ItemType.ITEM, null, "arrow")
                .setFromPocketId(SpecialPocket.loot.getId())
                .setToPocketId("backpack-1"));

        // Create backpack 2
        req.add(new Modification()
                .create(Posting.ItemType.POCKET, "backpack-2", "backpack")
                .setFromPocketId(SpecialPocket.start.getId()));

        // MOVE arrow from backpack-2 to backpack-1
        req.add(new Modification()
                .move("arrow", 1L, "backpack-1", "backpack-2"));

        ModificationResponse resp = modifyPockets.modifyPockets(req);
        System.out.println(Transform.toJsonString(resp));

        Pocket b1 = Pocket.findByNaturalId(profile, "backpack-1");
        Pocket b2 = Pocket.findByNaturalId(profile, "backpack-2");
        Item i = Item.findByNaturalId(profile, "arrow");

        Util.checkPocketItemLinks(b1, 1, i, 2, 0);
        Util.checkPocketItemLinks(b2, 1, i, 2, 1);

        assertThat(profile.getJournals()).hasSize(3);

        Journal j1 = profile.getJournals().get(2);
        assertThat(j1.datetime).isEqualTo(req.datetime);

        List<Posting> jMods = j1.changes;
        assertThat(jMods).hasSize(9);

        // A create step...
        Util.checkCreatePosting(jMods.get(0), Posting.ItemType.POCKET, "backpack-1", b1);
        // Remove from special pocket
        Util.checkPosting(jMods.get(1), Posting.PostingType.REMOVE, Posting.ItemType.POCKET, "p:start", "backpack-1", 1L);

        // A create step...
        Util.checkCreatePosting(jMods.get(2), Posting.ItemType.ITEM, "arrow", i);
        // Remove from special pocket
        Util.checkPosting(jMods.get(3), Posting.PostingType.REMOVE, Posting.ItemType.ITEM, "p:loot", "arrow", 1L);
        // Then the add step
        Util.checkPosting(jMods.get(4), Posting.PostingType.ADD, Posting.ItemType.ITEM, "backpack-1", "arrow", 1L);

        // A create step...
        Util.checkCreatePosting(jMods.get(5), Posting.ItemType.POCKET, "backpack-2", b2);
        // Remove from special pocket
        Util.checkPosting(jMods.get(6), Posting.PostingType.REMOVE, Posting.ItemType.POCKET, "p:start", "backpack-2", 1L);

        // Remove from the first backpack
        Util.checkPosting(jMods.get(7), Posting.PostingType.REMOVE, Posting.ItemType.ITEM, "backpack-1", "arrow", 1L);
        // Add to the second
        Util.checkPosting(jMods.get(8), Posting.PostingType.ADD, Posting.ItemType.ITEM, "backpack-2", "arrow", 1L);
    }

    @Test
    @TestTransaction
    public void testAddMoreAndMove() {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        // If you _remove_ an item (not use up some quantity)
        ModificationRequest req = new ModificationRequest("Adding things", "1499-03-24");

        // Create item and add to haversack
        req.add(new Modification()
                .create(Posting.ItemType.ITEM, null, "arrow")
                .setFromPocketId(SpecialPocket.start.getId())
                .setToPocketId("haversack"));

        // Add 30 more arrows
        req.add(new Modification()
                .add("arrow", 30L, "haversack"));

        // Move 27 to another pocket
        req.add(new Modification()
                .move("arrow", 27L, "haversack", "backpack"));

        modifyPockets.modifyPockets(req);

        Pocket h = Pocket.findByNaturalId(profile, "haversack");
        Pocket b = Pocket.findByNaturalId(profile, "backpack");
        Item i = Item.findByNaturalId(profile, "arrow");

        Util.checkPocketItemLinks(h, 1, i, 2, 4);
        // backpack also contains rations (import.sql)
        Util.checkPocketItemLinks(b, 2, i, 2, 27);

        Journal j1 = profile.getJournals().get(2);
        List<Posting> postings = j1.changes;
        assertThat(postings).hasSize(6);

        // A create step...
        Util.checkCreatePosting(postings.get(0), Posting.ItemType.ITEM, "arrow", i);
        Util.checkPosting(postings.get(1), Posting.PostingType.REMOVE, Posting.ItemType.ITEM, "p:start", "arrow", 1L);

        // Then the add step
        Util.checkPosting(postings.get(2), Posting.PostingType.ADD, Posting.ItemType.ITEM, "haversack", "arrow", 1L);
        // Then add more
        Util.checkPosting(postings.get(3), Posting.PostingType.ADD, Posting.ItemType.ITEM, "haversack", "arrow", 30L);
        // A remove step
        Util.checkPosting(postings.get(4), Posting.PostingType.REMOVE, Posting.ItemType.ITEM, "haversack", "arrow", 27L);
        // Then the add step
        Util.checkPosting(postings.get(5), Posting.PostingType.ADD, Posting.ItemType.ITEM, "backpack", "arrow", 27L);
    }

    @Test
    @TestTransaction
    public void testAddCustomItemToPocket() {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        // If you _remove_ an item (not use up some quantity)
        ModificationRequest req = new ModificationRequest("Adding things", "1499-03-24");
        req.add(new Modification()
                .add("arrow-of-dedona", 1L, "haversack")
                .setFromPocketId(SpecialPocket.loot.getId())
                .setItemDetails(new ItemDetails()
                        .setName("Arrow of Dedona")
                        .setWeight(0.3)
                        .setNotes("Talking arrow. Don't shoot it")
                        .setTradable(false)
                        .setBaseUnitValue(3000.0)));
        modifyPockets.modifyPockets(req);

        Pocket p = Pocket.findByNaturalId(profile, "haversack");
        Item i = Item.findByNaturalId(profile, "arrow-of-dedona");

        Util.checkPocketItemLinks(p, i, 1);

        assertThat(profile.getJournals()).hasSize(3);
        Journal j1 = profile.getJournals().get(2);
        List<Posting> postings = j1.changes;
        assertThat(postings).hasSize(2);

        // A create step...
        Util.checkCreatePosting(postings.get(0), Posting.ItemType.ITEM, "arrow-of-dedona", i);
        // Then the add step
        Util.checkPosting(postings.get(1), Posting.PostingType.ADD, Posting.ItemType.ITEM, "haversack", "arrow-of-dedona", 1L);
    }

    @Test
    @TestTransaction
    public void testRemoveItemFromPocket() {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        // If you _remove_ an item (not use up some quantity)
        ModificationRequest req = new ModificationRequest("Removing things", "1499-03-24");

        req.add(new Modification()
                .remove("rations-1-day", 1L, "backpack"));

        ModificationResponse resp = modifyPockets.modifyPockets(req);
        System.out.println(Transform.toJsonString(resp));

        Pocket p = Pocket.findByNaturalId(profile, "backpack");
        Item i = Item.findByNaturalId(profile, "rations-1-day");

        PocketItem p_i = Util.checkPocketItemLinks(p, i, 9);
        assertThat(p_i.cumulativeValue).isEqualTo(450);
        assertThat(p_i.cumulativeWeight).isEqualTo(18);

        Journal j1 = profile.getJournals().get(2);
        List<Posting> postings = j1.changes;
        assertThat(postings).hasSize(1);

        Util.checkPosting(postings.get(0), Posting.PostingType.REMOVE,
                Posting.ItemType.ITEM, "backpack", "rations-1-day", 1L);
    }

    @Test
    @TestTransaction
    public void testAddRemovePocketChange() {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        // If you _remove_ an item (not use up some quantity)
        ModificationRequest req = new ModificationRequest("Add currency", "1499-03-24");

        req.add(new Modification()
                .add(Posting.ItemType.CURRENCY, "11sp 5gp 3pp 107cp", null, "haversack")
                .setFromPocketId(SpecialPocket.loot.getId()));

        ModificationResponse resp = modifyPockets.modifyPockets(req);
        System.out.println(Transform.toJsonString(resp));

        Pocket h = Pocket.findByNaturalId(profile, "haversack");
        Pocket b = Pocket.findByNaturalId(profile, "backpack");

        // haversack has 4 coins. each type of coin is in 1 pocket
        Util.checkPocketCurrencyLinks(profile, h, 4, "pp", 3);
        Util.checkPocketCurrencyLinks(profile, h, 4, "gp", 5);
        Util.checkPocketCurrencyLinks(profile, h, 4, "sp", 11);
        Util.checkPocketCurrencyLinks(profile, h, 4, "cp", 107);

        req = new ModificationRequest("Move currency", "1499-03-24");
        req.add(new Modification()
                .move("5sp 2gp 1pp 72cp", null, "haversack", "backpack")
                .setItemType(Posting.ItemType.CURRENCY));

        resp = modifyPockets.modifyPockets(req);
        System.out.println(Transform.toJsonString(resp));

        // haversack has 4 coins, backpack has 4 coins
        Util.checkPocketCurrencyLinks(profile, h, 4, "pp", 2);
        Util.checkPocketCurrencyLinks(profile, h, 4, "gp", 3);
        Util.checkPocketCurrencyLinks(profile, h, 4, "sp", 6);
        Util.checkPocketCurrencyLinks(profile, h, 4, "cp", 35);

        // backpack already has 2gp ..
        Util.checkPocketCurrencyLinks(profile, b, 4, "pp", 1);
        Util.checkPocketCurrencyLinks(profile, b, 4, "gp", 4);
        Util.checkPocketCurrencyLinks(profile, b, 4, "sp", 5);
        Util.checkPocketCurrencyLinks(profile, b, 4, "cp", 72);

        req = new ModificationRequest("Remove currency", "1499-03-24");
        req.add(new Modification()
                .remove(Posting.ItemType.CURRENCY, "*pp *gp *sp *cp", null, "haversack"));

        resp = modifyPockets.modifyPockets(req);
        System.out.println(Transform.toJsonString(resp));

        // haversack still has 4 coins; each type of coin is in 2 pockets
        Util.checkPocketCurrencyLinks(profile, h, 4, "pp", 0);
        Util.checkPocketCurrencyLinks(profile, h, 4, "gp", 0);
        Util.checkPocketCurrencyLinks(profile, h, 4, "sp", 0);
        Util.checkPocketCurrencyLinks(profile, h, 4, "cp", 0);
    }
}
