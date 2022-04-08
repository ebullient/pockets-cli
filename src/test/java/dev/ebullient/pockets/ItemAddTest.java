package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(result.getOutput()).contains(
            "(1) Jeweled Eyepatch [6] added to Backpack [2]");

        assertThat(result.getOutput()).doesNotContain(
            "Backpack [2] contains", // verbose
            "[   5] ( 10)  Rations", // verbose
            "This backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch", "--trade" })
    public void testItemAddTrade(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "(1) Jeweled Eyepatch [6] added to Backpack [2]",
            "Backpack [2] contains", // verbose
            "[   6] (  1)  Jeweled Eyepatch                                        -   30.0   ",
            "[   5] ( 10)  Rations", // verbose
            "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch", "--no-trade" })
    public void testItemAddNoTrade(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "(1) Jeweled Eyepatch [6] added to Backpack [2]",
            "Backpack [2] contains", // verbose
            "[   5] ( 10)  Rations", // verbose
            "[   6] (  1)  Jeweled Eyepatch                                        -   30.0  🔒",
            "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch(value = { "add", "12", "Jeweled Eyepatch" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemAddUnknownPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "The specified value [12] doesn't match any of your pockets.",
            "Your pockets:");
    }

    @Test
    @Launch({"a", "--help"})
    public void testItemAddHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "Add an item to a pocket",
            "Usage: pockets a ");
    }
}
