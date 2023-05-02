package dev.ebullient.pockets;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ItemAddTest {
    @Test
    @Launch({ "add", "2", "Jeweled Eyepatch", "--brief" })
    public void testItemAdd(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "(1) Jeweled Eyepatch [8] added to Backpack [2]");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "Backpack [2] contains", // verbose
                "[   5] ( 10)  Rations", // verbose
                "This backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch", "--trade" })
    public void testItemAddTrade(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "(1) Jeweled Eyepatch [8] (30.0 gp) added to Backpack [2]",
                "Backpack [2] contains:", // verbose
                "[   8] (  1)  Jeweled Eyepatch   -     30.0   ",
                "[   5] ( 10)  Rations", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch", "-w", "0.01", "--no-trade" })
    public void testItemAddNoTrade(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "(1) Jeweled Eyepatch [8] (0.01 lbs) (30.0 gp) added to Backpack [2]",
                "Backpack [2] contains", // verbose
                "[   5] ( 10)  Rations", // verbose
                "[   8] (  1)  Jeweled Eyepatch  0.01     30.0  🔒",
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch(value = { "add", "12", "Jeweled Eyepatch" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemAddUnknownPocket(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "The specified value [12] doesn't match any of your pockets.",
                "Your pockets:");
    }

    @Test
    @Launch({ "a", "--help" })
    public void testItemAddHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Add an item to a pocket",
                "Usage: pockets a ");
    }
}
