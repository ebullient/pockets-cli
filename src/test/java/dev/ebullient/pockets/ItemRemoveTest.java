package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ItemRemoveTest {
    @Test
    @Launch({ "r", "2", "rations", "--debug", "--brief" })
    public void testRemoveByNameBrief(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(10) Rations [5] have been removed from Backpack [2]");

        assertThat(result.getOutput()).doesNotContain(
                "Backpack [2] is empty.", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "r", "2", "5", "--debug" })
    public void testRemoveById(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(10) Rations [5] have been removed from Backpack [2]",
                "Backpack [2] is empty.", // verbose
                "This Backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "r", "--help" })
    public void testItemRemoveHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Remove an item from a pocket",
                "Usage: pockets r ");
    }
}
