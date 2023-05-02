package dev.ebullient.pockets;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketDeleteTest {
    @Test
    @Launch({ "d", "1" })
    public void testPocketDeleteCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "✅ Coins [1] has been deleted.");
    }

    @Test
    @Launch(value = { "delete", "backpack" }, exitCode = PocketTui.NOT_FOUND)
    public void testPocketDeleteCommandDuplicate(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "[   2] 🎒  Backpack",
                "[   4] 🎒  Backpack",
                "The specified value [backpack] matches more than one pocket.");
    }

    @Test
    @Launch({ "d", "--help" })
    public void testPocketDeleteHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Delete a pocket",
                "Usage: pockets d ");
    }
}
