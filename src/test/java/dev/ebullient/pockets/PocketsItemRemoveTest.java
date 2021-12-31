package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
@TestTransaction
public class PocketsItemRemoveTest {

    @Test
    @Launch({ "r", "2", "Rations", "--force", "--brief" })
    public void testRemoveByName(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(10) Rations [5] have been removed from Backpack [2]");

        assertThat(result.getOutput()).doesNotContain(
                "Backpack [2] is empty.", // verbose
                "This backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "remove", "2", "Rations", "-f" })
    public void testRemoveByNameVerbose(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Backpack [2] contains (10) Rations [5]", // verbose
                "(10) Rations [5] have been removed from Backpack [2]",
                "Backpack [2] is empty.", // verbose
                "This backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "remove", "2", "5", "-bf" })
    public void testRemoveById(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(10) Rations [5] have been removed from Backpack [2]");
    }

    @Test
    @Launch(value = { "r", "12", "Rations" }, exitCode = 2)
    public void testRemoveUnknownPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "The specified value [12] doesn't match any of your pockets.",
                "Your pockets:");
    }
}
