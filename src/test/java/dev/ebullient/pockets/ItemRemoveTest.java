package dev.ebullient.pockets;

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
        Util.assertConciseContentContains(result.getOutputStream(),
                "(10) Rations [5] have been removed from Backpack [2]");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "Backpack [2] is empty.", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "r", "2", "5", "--debug" })
    public void testRemoveById(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "(10) Rations [5] have been removed from Backpack [2]",
                "Backpack [2] is empty.", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "r", "1", "silver-sp", "--debug" })
    public void testRemoveSingle(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Silver (sp) [7] has been removed from Coins [1]",
                "This Pouch weighs 1 pound when empty."); // verbose
    }

    @Test
    @Launch(value = { "r", "12", "Rations" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemRemoveUnknownPocket(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "The specified value [12] doesn't match any of your pockets.",
                "Your pockets:");
    }

    @Test
    @Launch(value = { "r", "2", "Things" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemRemoveUnknownItem(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "'Things' doesn't match any of the items in your pocket.",
                "Backpack [2] contains:");
    }

    @Test
    @Launch({ "r", "--help" })
    public void testItemRemoveHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Remove an item from a pocket",
                "Usage: pockets r ");
    }
}
