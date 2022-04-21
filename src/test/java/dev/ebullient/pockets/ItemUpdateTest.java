package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ItemUpdateTest {
    @Test
    @Launch({ "u", "2", "rations" })
    public void testItemUpdateCommand(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] was not updated (no changes).");
    }

    @Test
    @Launch(value = { "u", "2", "Rations", "--quantity", "15", "--brief" })
    public void testItemUpdateBrief(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] has been updated.");
    }

    @Test
    @Launch({ "update", "2", "rations", "-q", "15000", "-v", "2sp", "-w", "2.0", "--trade" })
    public void testItemUpdateByNameTrade(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Rations [5] has been updated",
                "[ ID ] (   Q )  Name / Description", // adjusted column width
                "[   5] (15000)  Rations");
    }

    @Test
    @Launch({ "u", "--help" })
    public void testItemUpdateHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Update an item in a pocket",
                "Usage: pockets u ");
    }
}
