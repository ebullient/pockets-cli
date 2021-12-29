package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsListTest {

    @Test
    @Launch({ "list", "--verbose" })
    public void testListAllPockets(LaunchResult result) {
        assertThat(result.getOutput()).contains("Your pockets:", "Coins", "Backpack", "Haversack");
    }

    @Test
    @Launch({ "list", "2", "--verbose" })
    public void testListPocketById(LaunchResult result) {
        assertThat(result.getOutput()).contains("Backpack [2] contains:", "(  10) Rations");
    }

    @Test
    @Launch({ "list", "Coins", "--verbose" })
    public void testListPocketByName(LaunchResult result) {
        assertThat(result.getOutput()).contains("Coins [1] is empty.");
    }

    @Test
    @Launch(value = { "list", "backpack", "--verbose" }, exitCode = 2)
    public void testListBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "[   2] 🎒 Backpack",
                "[   4] 🎒 Backpack",
                "Unable to choose a pocket. Please be more specific.");
    }
}
