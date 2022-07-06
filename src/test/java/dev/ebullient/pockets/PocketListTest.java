package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketListTest {
    @Test
    @Launch({ "l" })
    public void testListCommand() {
    }

    @Test
    @Launch({ "l", "1" })
    public void testListPocketCommand(LaunchResult result) {
        assertThat(Util.noWhitespace(result.getOutput())).contains(
                Util.noWhitespace("👛 Coins [1] contains:"),
                Util.noWhitespace("[   6] ( 50)  Gold (gp)     0.02     1.0   "),
                Util.noWhitespace("[   7] (  1)  Silver (sp)   0.02     0.1   "),
                Util.noWhitespace("This Pouch weighs 1 pound when empty."));
    }

    @Test
    @Launch({ "l", "haversack" })
    public void testListHaversackCommand(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Haversack [3] is empty.",
                "This Handy Haversack is magical.");
    }

    @Test
    @Launch({ "l", "--help" })
    public void testListHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "What do we have in our pockets?",
                "Usage: pockets l ");
    }
}
