package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ItemRemoveTest {
    @Test
    @Launch({ "r", "2", "rations", "--debug", "--brief" })
    public void testRemoveByNameBrief(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(10) Rations [5] have been removed from Backpack [2]");

        assertThat(result.getOutput()).doesNotContain(
                "Backpack [2] is empty.", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "r", "2", "5", "--debug" })
    public void testRemoveById(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(10) Rations [5] have been removed from Backpack [2]",
                "Backpack [2] is empty.", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "r", "1", "silver-sp", "--debug" })
    public void testRemoveSingle(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Silver (sp) [7] has been removed from Coins [1]",
                "This Pouch weighs 1 pound when empty."); // verbose
    }

    @Test
    @Launch(value = { "r", "12", "Rations" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemRemoveUnknownPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "The specified value [12] doesn't match any of your pockets.",
                "Your pockets:");
    }

    @Test
    @Launch(value = { "r", "2", "Things" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemRemoveUnknownItem(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "'Things' doesn't match any of the items in your pocket.",
                "Backpack [2] contains:");
    }

    @Test
    @Launch({ "r", "--help" })
    public void testItemRemoveHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Remove an item from a pocket",
                "Usage: pockets r ");
    }
}
