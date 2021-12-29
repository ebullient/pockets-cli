package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsDeleteTest {
    @Test
    @Launch(value = { "delete", "backpack", "--verbose" }, exitCode = 2)
    public void testDeleteBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "[   2] 🎒 Backpack",
                "[   4] 🎒 Backpack",
                "Unable to choose a pocket. Please be more specific.");
    }

    @Test
    @Launch({ "delete", "2", "--verbose", "--force" })
    public void testDeleteSpecificBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "✅ Backpack [2] has been deleted.");
    }
}
