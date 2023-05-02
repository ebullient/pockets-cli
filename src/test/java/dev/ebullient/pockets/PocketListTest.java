package dev.ebullient.pockets;

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
        Util.assertConciseContentContains(result.getOutputStream(),
                "👛 Coins [1] contains:",
                "[   6] ( 50)  Gold (gp)     0.02     1.0   ",
                "[   7] (  1)  Silver (sp)   0.02     0.1   ",
                "This Pouch weighs 1 pound when empty.");
    }

    @Test
    @Launch({ "l", "haversack" })
    public void testListHaversackCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Haversack [3] is empty.",
                "This Handy Haversack is magical.");
    }

    @Test
    @Launch({ "l", "--help" })
    public void testListHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "What do we have in our pockets?",
                "Usage: pockets l ");
    }
}
