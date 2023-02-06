package dev.ebullient.pockets.actions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.ebullient.pockets.Util;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Journal;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.Pocket.SpecialPocket;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class AddRemovePocketTest {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    ProfileContext profileContext;

    @Test
    @TestTransaction
    public void testCreatePocketBadProfile() {
        Assertions.assertThrows(NoResultException.class, () -> profileContext.setActiveProfile("Nonsense"));
    }

    @Test
    @TestTransaction
    public void testCreatePocket() {
        profileContext.setActiveProfile("default");

        ModificationRequest req = new ModificationRequest("New backpack", "1499-03-24");
        req.add(new Modification()
                .create(Posting.ItemType.POCKET, null, "backpack")
                .setItemDetails(new PocketDetails().setName("Backpack of Doom"))
                .setFromPocketId(SpecialPocket.start.getId()));

        modifyPockets.modifyPockets(req);

        Profile profile = profileContext.getActiveProfile();
        assertThat(profile.getJournals()).hasSize(1);
        Journal j1 = profile.getJournals().get(0);
        assertThat(j1.datetime).isEqualTo(req.datetime);

        Pocket p = Pocket.findByNaturalId(profile, "backpack-of-doom");
        assertThat(p).isNotNull();
        assertThat(p.slug).isEqualTo("backpack-of-doom");
        assertThat(p.emoji).isEqualTo("🎒");
        assertThat(p.pocketDetails.baseUnitValue).isEqualTo(200);

        Util.checkCreatePosting(j1.changes.get(0), ItemType.POCKET, "backpack-of-doom", p);

        ModificationRequest fail = new ModificationRequest("Add a backpack we already have", "1499-03-24");
        fail.add(new Modification()
                .create(Posting.ItemType.POCKET, null, "backpack")
                .setItemDetails(new PocketDetails().setName("Backpack of Doom"))
                .setFromPocketId(SpecialPocket.start.getId()));

        // This should fail -- duplicate pockets
        Assertions.assertThrows(InvalidPocketState.class, () -> modifyPockets.modifyPockets(fail));
    }

    @Test
    @TestTransaction
    public void testRemovePocket() {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        ModificationRequest req = new ModificationRequest("Remove a pocket", "1499-03-24");
        req.add(new Modification()
                .remove(Posting.ItemType.POCKET, "backpack", 1L, null));

        modifyPockets.modifyPockets(req);

        assertThat(profile.getPocket("backpack")).isNull();

        Item i = profile.getItem("rations-1-day");
        assertThat(i).isNotNull();
        assertThat(i.pocketItems).isEmpty();
    }

    @Test
    @TestTransaction
    public void testAddPocketToPocketRemoveBoth() throws JsonProcessingException {
        profileContext.setActiveProfile("test-5e");
        Profile profile = profileContext.getActiveProfile();

        ModificationRequest req = new ModificationRequest("New backpack", "1499-03-24");
        req.add(new Modification()
                .create(Posting.ItemType.POCKET, null, "backpack")
                .setItemDetails(new PocketDetails().setName("Backpack of Doom"))
                .setToPocketId("backpack")
                .setFromPocketId(SpecialPocket.start.getId()));

        modifyPockets.modifyPockets(req);
        assertThat(profile.getJournals()).hasSize(3);

        Pocket backpack = Pocket.findByNaturalId(profile, "backpack");
        Pocket backpackDoom = Pocket.findByNaturalId(profile, "backpack-of-doom");

        assertThat(backpack.id).isNotNull();
        assertThat(backpackDoom.id).isNotNull();
        assertThat(backpack.nestedPockets).contains(backpackDoom);
        assertThat(backpackDoom.parentPocket).isEqualTo(backpack);

        Journal journal = profile.getJournals().get(2);
        List<Posting> mods = journal.changes;
        assertThat(mods).hasSize(3);

        // A create step...
        Util.checkCreatePosting(mods.get(0), ItemType.POCKET, "backpack-of-doom", backpackDoom);
        Util.checkPosting(mods.get(1), PostingType.REMOVE, ItemType.POCKET, "p:start", "backpack-of-doom", 1L);
        // Then the add step
        Util.checkPosting(mods.get(2), PostingType.ADD, ItemType.POCKET, "backpack", "backpack-of-doom", 1L);

        req = new ModificationRequest("New backpack", "1499-03-24");
        req.add(new Modification()
                .remove(ItemType.POCKET, "backpack", null, null));

        modifyPockets.modifyPockets(req);

        assertThat(profile.getJournals()).hasSize(4);
        assertThat(profile.getPocket("backpack-of-doom")).isNull();
        assertThat(profile.getPocket("backpack")).isNull();

        mods = profile.getJournals().get(3).changes;
        // A remove
        Util.checkPosting(mods.get(0), PostingType.REMOVE, ItemType.POCKET, null, "backpack", 1L);
    }
}
