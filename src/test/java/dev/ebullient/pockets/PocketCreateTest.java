package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketCreateTest {

    @Test
    @Launch({ "create", "backpack", "Jellybeans" })
    public void testPocketCreateBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Jellybeans has been created with id",
                "This Backpack weighs 5.0 pounds when empty.",
                "It can hold 30.0 pounds or 1 cubic foot of gear.");
    }

    @Test
    @Launch({ "c", "backpack", "Magic beans", "--magic" })
    public void testCreateMagicBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Magic beans has been created with id",
                "This Backpack is magical.",
                "It always weighs 5.0 pounds, regardless of its contents.",
                "It can hold 30.0 pounds or 1 cubic foot of gear.");
    }

    @Test
    @Launch({ "c", "bag-of-holding", "Lovely Day" })
    public void testCreateBagOfHolding(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Lovely Day has been created with id",
                "This Bag of Holding is magical.",
                "It always weighs 15.0 pounds, regardless of its contents.",
                "It can hold 500.0 pounds or 64.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "c", "basket" })
    public void testCreateBasket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Basket has been created with id",
                "This Basket weighs 2.0 pounds when empty.",
                "It can hold 40.0 pounds or 2.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "chest" })
    public void testCreateChest(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Chest has been created with id",
                "This Chest weighs 25.0 pounds when empty.",
                "It can hold 300.0 pounds or 12.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "c", "crossbow-bolt-case" })
    public void testCreateCrossbowBoltCase(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Crossbow Bolt Case has been created with id",
                "This Crossbow Bolt Case weighs 1 pound when empty.",
                "It can hold 2.5 pounds of gear.",
                "20 crossbow bolts");
    }

    @Test
    @Launch({ "create", "efficient-quiver" })
    public void testCreateEfficientQuiver(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Efficient Quiver has been created with id",
                "This Efficient Quiver is magical.",
                "It always weighs 2.0 pounds, regardless of its contents.",
                "There are 3 pockets which may contain:",
                "1: 60 crossbow bolts, arrows, or similar");
    }

    @Test
    @Launch({ "create", "handy-haversack", "Toast tastes great in the morning!" })
    public void testCreateHaversack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Toast tastes great in the morning! has been created with id",
                "This Handy Haversack is magical.",
                "It always weighs 5.0 pounds, regardless of its contents.",
                "It can hold 120.0 pounds or 12.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "portable-hole", "Summoner's", "Gift" })
    public void testCreatePortableHole(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Summoner's Gift has been created with id",
                "This Portable Hole is magical.",
                "It always weighs 0.0 pounds, regardless of its contents.",
                "It can hold 282.7 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "pouch" })
    public void testCreatePouch(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pouch has been created with id",
                "This Pouch weighs 1 pound when empty.",
                "It can hold 6.0 pounds or 0.2 cubic feet of gear");
    }

    @Test
    @Launch({ "create", "sack", "Bag", "of", "the", "day" })
    public void testCreateSack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Bag of the day has been created with id",
                "This Sack weighs 0.5 pounds when empty.",
                "It can hold 30.0 pounds or 1 cubic foot of gear.");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--no-magic",
            "-n", "This pocket can only hold elephants." })
    public void testCreateCustomPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pocket (custom) has been created with id",
                "This Pocket (custom) weighs 1 pound when empty.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear",
                "This pocket can only hold elephants.");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--no-magic", "Fitzherbert's wish" })
    public void testCreateCustomNamedPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Fitzherbert's wish has been created with id",
                "This Pocket (custom) weighs 1 pound when empty.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--magic" })
    public void testCreateMagicalCustomPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pocket (custom) has been created with id",
                "This Pocket (custom) is magical.",
                "It always weighs 1 pound, regardless of its contents.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "c", "--types" })
    public void testPocketCreateTypesCommand(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "bag-of-holding", "Bag of Holding");
    }

    @Test
    @Launch({ "c", "--help" })
    public void testPocketCreateHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Create a new pocket",
                "Usage: pockets c ");
    }
}
