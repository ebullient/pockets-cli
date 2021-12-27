package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsListTest {

    @Test
    @Launch({ "list" })
    public void testListAllPockets(LaunchResult result) {
        assertThat(result.getOutput()).contains("Your pockets:", "Coins", "Backpack", "Haversack");
    }

    @Test
    @Launch({ "list", "2" })
    public void testListPocketById(LaunchResult result) {
        assertThat(result.getOutput()).contains("Backpack [2] contains:", "( 10)  Rations");
    }

    @Test
    @Launch({ "list", "Coins" })
    public void testListPocketByName(LaunchResult result) {
        assertThat(result.getOutput()).contains("Coins [1] is empty.");
    }
}
