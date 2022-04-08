package dev.ebullient.pockets;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusMainTest
public class PocketDeleteTest {
    @Test
    @Launch({"l"})
    public void testListCommand() {
    }

    @Test
    @Launch({"l", "1"})
    public void testListPocketCommand(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "👛 Coins [1] is empty.",
            "This Pouch weighs 1 pound when empty.");
    }

    @Test
    @Launch({"l", "--help"})
    public void testListHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "What do we have in our pockets?",
            "Usage: pockets l ");
    }
}
