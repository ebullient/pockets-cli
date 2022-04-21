package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketEditTest {
    @Test
    @Launch(value = { "edit", "backpack" }, exitCode = PocketTui.NOT_FOUND)
    public void testPocketEditBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "[   2] 🎒  Backpack",
                "[   4] 🎒  Backpack",
                "The specified value [backpack] matches more than one pocket.");
    }

    @Test
    @Launch({ "edit", "2", "--magic" })
    public void testPocketEditSpecificBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "This Backpack is magical.");
    }

    @Test
    @Launch({ "e", "--help" })
    public void testPocketEditHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Edit a pocket",
                "Usage: pockets e ");
    }
}
