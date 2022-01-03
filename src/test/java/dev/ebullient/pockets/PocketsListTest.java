package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsListTest {

    @BeforeAll
    static void beforeAll() throws IOException {
        Files.deleteIfExists(Path.of("target/.pockets/cache.json"));
    }

    @Test
    @Launch({ "list" })
    public void testListAllPockets(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Your pockets:",
                "Coins",
                "Backpack",
                "*Haversack");
    }

    @Test
    @Launch({ "list", "2" })
    public void testListPocketById(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Backpack [2] contains:",
                "( 10)  Rations");
    }

    @Test
    @Launch({ "list", "Coins" })
    public void testListPocketByName(LaunchResult result) {
        assertThat(result.getOutput()).contains("Coins [1] is empty.");
    }

    @Test
    @Launch(value = { "list", "backpack" }, exitCode = 2)
    public void testListBackpack(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "[   2] 🎒  Backpack",
                "[   4] 🎒  Backpack",
                "Unable to choose a pocket. Please be more specific.");
    }
}
