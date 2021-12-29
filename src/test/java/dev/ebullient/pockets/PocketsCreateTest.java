package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsCreateTest {

    @Test
    @Launch({ "create", "backpack", "Jellybeans", "--verbose" })
    public void testCreateBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Jellybeans has been created with id",
                "This backpack weighs 5.0 pounds when empty.",
                "It can hold 30.0 pounds or 1.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "backpack", "Magic beans", "--magic", "--verbose" })
    public void testCreateMagicBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Magic beans has been created with id",
                "This backpack is magical. It always weighs 5.0 pounds, regardless of its contents.",
                "It can hold 30.0 pounds or 1.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "pouch", "--verbose" })
    public void testCreatePouch(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pouch has been created with id",
                "This pouch weighs 1.0 pounds when empty.",
                "It can hold 6.0 pounds or 0.2 cubic feet of gear");
    }

    @Test
    @Launch({ "create", "haversack", "Toast tastes great in the morning!", "--verbose" })
    public void testCreateHaversack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Toast tastes great in the morning! has been created with id",
                "This Handy Haversack is magical. It always weighs 5.0 pounds, regardless of its contents.",
                "It can hold 120.0 pounds or 12.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "BagOfHolding", "Lovely Day", "--verbose" })
    public void testCreateBagOfHolding(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Lovely Day has been created with id",
                "This Bag of Holding is magical. It always weighs 15.0 pounds, regardless of its contents.",
                "It can hold 500.0 pounds or 64.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "portableHole", "Summoner's", "Gift", "--verbose" })
    public void testCreatePortableHole(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Summoner's Gift has been created with id",
                "This Portable Hole is magical. It always weighs 0.0 pounds, regardless of its contents.",
                "It can hold 282.7 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "sack", "Bag", "of", "the", "day", "--verbose" })
    public void testCreateSack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Bag of the day has been created with id",
                "This sack weighs 0.5 pounds when empty.",
                "It can hold 30.0 pounds or 1.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--no-magic", "--verbose" })
    public void testCreateCustomPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pocket has been created with id",
                "This pocket weighs 1.0 pounds when empty.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--magic", "--verbose" })
    public void testCreateMagicalCustomPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pocket has been created with id",
                "This pocket is magical. It always weighs 1.0 pounds, regardless of its contents.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear.");
    }
}
