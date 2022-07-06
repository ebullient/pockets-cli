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
                "(1) Jeweled Eyepatch [8] added to Backpack [2]");

        assertThat(Util.noWhitespace(result.getOutput())).doesNotContain(
                Util.noWhitespace("Backpack [2] contains"), // verbose
                Util.noWhitespace("[   5] ( 10)  Rations"), // verbose
                Util.noWhitespace("This backpack weighs 5.0 pounds when empty.")); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch", "--trade" })
    public void testItemAddTrade(LaunchResult result) {
        assertThat(Util.noWhitespace(result.getOutput())).contains(
                Util.noWhitespace("(1) Jeweled Eyepatch [8] (30.0 gp) added to Backpack [2]"),
                Util.noWhitespace("Backpack [2] contains:"), // verbose
                Util.noWhitespace("[   8] (  1)  Jeweled Eyepatch   -     30.0   "),
                Util.noWhitespace("[   5] ( 10)  Rations"), // verbose
                Util.noWhitespace("This Backpack weighs 5.0 pounds when empty.")); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch", "-w", "0.01", "--no-trade" })
    public void testItemAddNoTrade(LaunchResult result) {
        assertThat(Util.noWhitespace(result.getOutput())).contains(
                Util.noWhitespace("(1) Jeweled Eyepatch [8] (0.01 lbs) (30.0 gp) added to Backpack [2]"),
                Util.noWhitespace("Backpack [2] contains"), // verbose
                Util.noWhitespace("[   5] ( 10)  Rations"), // verbose
                Util.noWhitespace("[   8] (  1)  Jeweled Eyepatch  0.01     30.0  🔒"),
                Util.noWhitespace("This Backpack weighs 5.0 pounds when empty.")); // verbose
    }

    @Test
    @Launch(value = { "add", "12", "Jeweled Eyepatch" }, exitCode = PocketTui.NOT_FOUND)
    public void testItemAddUnknownPocket(LaunchResult result) {
        assertThat(Util.noWhitespace(result.getOutput())).contains(
                Util.noWhitespace("The specified value [12] doesn't match any of your pockets."),
                Util.noWhitespace("Your pockets:"));
    }

    @Test
    @Launch({ "a", "--help" })
    public void testItemAddHelp(LaunchResult result) {
        assertThat(Util.noWhitespace(result.getOutput())).contains(
                Util.noWhitespace("Add an item to a pocket"),
                Util.noWhitespace("Usage: pockets a "));
    }
}
