package dev.ebullient.pockets.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import dev.ebullient.pockets.config.ProfileConfigData;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ProfileLifecycleTest {

    @Test
    @TestTransaction
    public void testCreateProfile() {
        ProfileConfigData configData = ProfileConfigData.create("New profile");
        Profile.createProfile(configData);
        Util.assertProfiles(4);
    }

    @Test
    @TestTransaction
    public void testDeleteProfile() {
        // from import.sql
        Util.assertProfiles(3);
        Util.assertJournals(2);
        Util.assertPockets(5);
        Util.assertItems(2);
        Util.assertPocketItems(2);
        Util.assertPocketCurrency(2);

        String key = "test-5e";
        Profile p = Profile.findByNaturalId(key);
        assertThat(p.journals).hasSize(2);
        assertThat(p.pockets).hasSize(3);
        assertThat(p.items).hasSize(1);

        // when we delete the profile, it should cascade to "contained" elements
        Profile.deleteById(p.id);

        Util.assertProfiles(2);
        Util.assertJournals(0);
        Util.assertPockets(2);
        Util.assertItems(1);
        Util.assertPocketItems(1);
        Util.assertPocketCurrency(1);
    }

    @Test
    @TestTransaction
    public void testUpdateDeleteProfile() {
        // from import.sql
        Util.assertProfiles(3);
        Util.assertJournals(2);
        Util.assertPockets(5);
        Util.assertItems(2);
        Util.assertPocketItems(2);
        Util.assertPocketCurrency(2);

        String key = "test-pf2e";
        Profile p = Profile.findByNaturalId(key);
        assertThat(p.journals).hasSize(0);
        assertThat(p.pockets).hasSize(1);
        assertThat(p.items).hasSize(1);

        Pocket pocket = new Pocket()
                .setPocketDetails(p, "thing-with-stuff",
                        new PocketDetails().setRefId("backpack").setName("My stuff").setEmoji("🥡"));
        p.addPocket(pocket);

        Item i = new Item()
                .setItemDetails(p, "my-item",
                        new ItemDetails().setRefId("arrow").setName("Thing from person"));
        p.addItem(i);
        p.persistAndFlush();

        // PocketItem
        pocket.addItem(i, null);
        // PocketCurrency
        pocket.addCurrency("gp", 5);
        pocket.persistAndFlush();

        Util.assertPockets(6);
        Util.assertItems(3);
        Util.assertPocketItems(3);
        Util.assertPocketCurrency(3);

        // when we delete the profile, it should cascade to "contained" elements
        Profile.deleteById(p.id);

        Util.assertProfiles(2);
        Util.assertJournals(2); // journals from import.sql only
        Util.assertPockets(4);
        Util.assertItems(1);
        Util.assertPocketItems(1);
        Util.assertPocketCurrency(1);
    }

    @Test
    @TestTransaction
    public void testProfileReset() {
        // from import.sql
        Util.assertProfiles(3);
        Util.assertJournals(2);
        Util.assertPockets(5);
        Util.assertItems(2);
        Util.assertPocketItems(2);
        Util.assertPocketCurrency(2);

        Profile profile = Profile.findByNaturalId("default");
        profile.addPocket(new Pocket().setPocketDetails(profile, null,
                new PocketDetails().setRefId("portable-hole").setName("My thing").setEmoji("🥶")));

        profile.persist();

        // This is a reset rather than delete...
        Profile.deleteByNaturalId("default");

        Util.assertProfiles(3); // same number of pockets. Default not deleted
        Util.assertJournals(2);
        Util.assertPockets(4); // only one pocket from import.sql belongs to default
        Util.assertItems(2);
        Util.assertPocketItems(2);
        Util.assertPocketCurrency(2);
    }
}
