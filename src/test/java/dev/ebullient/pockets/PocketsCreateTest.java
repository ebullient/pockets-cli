package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsCreateTest {

    @Test
    @Launch({ "create", "backpack", "Jellybeans" })
    public void testCreateBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Jellybeans has been created with id",
                "This backpack weighs 5.0 pounds when empty.",
                "It can hold 30.0 pounds or 1 cubic foot of gear.");
    }

    @Test
    @Launch({ "create", "backpack", "Magic beans", "--magic" })
    public void testCreateMagicBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Magic beans has been created with id",
                "This backpack is magical.",
                "It always weighs 5.0 pounds, regardless of its contents.",
                "It can hold 30.0 pounds or 1 cubic foot of gear.");
    }

    @Test
    @Launch({ "create", "bag-of-holding", "Lovely Day" })
    public void testCreateBagOfHolding(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Lovely Day has been created with id",
                "This Bag of Holding is magical.",
                "It always weighs 15.0 pounds, regardless of its contents.",
                "It can hold 500.0 pounds or 64.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "basket" })
    public void testCreateBasket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Basket has been created with id",
                "This basket weighs 2.0 pounds when empty.",
                "It can hold 40.0 pounds or 2.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "chest" })
    public void testCreateChest(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Chest has been created with id",
                "This chest weighs 25.0 pounds when empty.",
                "It can hold 300.0 pounds or 12.0 cubic feet of gear.");
    }

    @Test
    @Launch({ "create", "crossbow-bolt-case" })
    public void testCreateCrossbowBoltCase(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Crossbow bolt case has been created with id",
                "This crossbow bolt case weighs 1 pound when empty.",
                "It can hold 2.5 pounds of gear.",
                "This wooden case can hold up to 20 crossbow bolts.");
    }

    @Test
    @Launch({ "create", "efficient-quiver" })
    public void testCreateEfficientQuiver(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Efficient Quiver has been created with id",
                "This Efficient Quiver is magical.",
                "It always weighs 2.0 pounds, regardless of its contents.",
                "This quiver has 3 compartments.");
    }

    @Test
    @Launch({ "create", "haversack", "Toast tastes great in the morning!" })
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
                "This pouch weighs 1 pound when empty.",
                "It can hold 6.0 pounds or 0.2 cubic feet of gear");
    }

    @Test
    @Launch({ "create", "sack", "Bag", "of", "the", "day" })
    public void testCreateSack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Bag of the day has been created with id",
                "This sack weighs 0.5 pounds when empty.",
                "It can hold 30.0 pounds or 1 cubic foot of gear.");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--no-magic" })
    public void testCreateCustomPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pocket has been created with id",
                "This pocket weighs 1 pound when empty.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear");
    }

    @Test
    @Launch({ "create", "custom", "-w", "3", "-v", "2", "-p", "1", "--magic" })
    public void testCreateMagicalCustomPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "A new pocket named Pocket has been created with id",
                "This pocket is magical.",
                "It always weighs 1 pound, regardless of its contents.",
                "It can hold 3.0 pounds or 2.0 cubic feet of gear.");
    }
}
