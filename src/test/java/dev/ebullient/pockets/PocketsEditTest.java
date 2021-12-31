package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsEditTest {
    @Test
    @Launch(value = { "edit", "backpack" }, exitCode = 2)
    public void testEditBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "[   2] 🎒 Backpack",
                "[   4] 🎒 Backpack",
                "Unable to choose a pocket. Please be more specific.");
    }

    @Test
    @Launch({ "edit", "2", "--force", "--magic" })
    public void testEditSpecificBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "This backpack is magical.");
    }
}
