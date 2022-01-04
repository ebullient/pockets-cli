package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
@TestTransaction
public class PocketItemUpdateTest {
    @BeforeAll
    static void beforeAll() throws IOException {
        Files.deleteIfExists(Path.of("target/.pockets/cache.json"));
    }

    @Test
    @Launch({ "update", "2", "rations", "-f" })
    public void testUpdateByNameVerboseNoChanges(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] was not updated. There are no changes.");
    }

    @Test
    @Launch(value = { "u", "2", "Rations", "--quantity", "15", "--brief" }, exitCode = 2)
    public void testUpdateNoConfirmation(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] was not updated");
    }

    @Test
    @Launch({ "update", "2", "rations", "-q", "15000", "-v", "2sp", "-w", "2.0", "-f", "--trade" })
    public void testUpdateByNameTrade(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] has been updated",
                "[ ID ] (    Q )  Name / Description", // adjusted column width
                "[   5] ( 15000)  Rations                                               2.0    0.2   ");
    }

    @Test
    @Launch({ "update", "2", "rations", "-q", "1500", "-v", "2sp", "-w", "2.0", "-f", "--no-trade" })
    public void testUpdateByNameNoTrade(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] has been updated",
                "[ ID ] (   Q )  Name / Description", // adjusted column width
                "[   5] ( 1500)  Rations                                               2.0    0.2  🔒");
    }

    @Test
    @Launch({ "update", "2", "rations", "--quantity", "15", "-w", "2.0", "-fb" })
    public void testUpdateByNameBrief(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] has been updated"); // verbose
        assertThat(result.getOutput()).doesNotContain(
                "Backpack [2] contains:"); // verbose
    }

    @Test
    @Launch({ "update", "2", "5", "-w", "2.0", "-bf" })
    public void testUpdateById(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] has been updated");
    }

    @Test
    @Launch(value = { "update", "12", "Rations" }, exitCode = 2)
    public void testUpdateUnknownPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "The specified value [12] doesn't match any of your pockets.",
                "Your pockets:");
    }

    @Test
    @Launch(value = { "update", "2", "Eyepatch" }, exitCode = 2)
    public void testUpdateUnknownPocketItem(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "'Eyepatch' doesn't match any of the items in your pocket.",
                "Backpack [2] contains:");
    }

}
