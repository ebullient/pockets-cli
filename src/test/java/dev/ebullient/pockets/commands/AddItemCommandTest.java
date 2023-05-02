package dev.ebullient.pockets.commands;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import picocli.CommandLine.ExitCode;

@QuarkusMainTest
public class AddItemCommandTest {

    @Test
    @Launch(value = { "-p=test-5e", "add" }, exitCode = ExitCode.USAGE)
    public void testAddPocketNoName(LaunchResult result) {
        Util.assertConciseContentContains(result.getErrorStream(),
                "Must specify the number, name, or id of an existing item (null).");
    }

    @Test
    @Launch(value = { "-p=test-5e", "add", "nonsense" }, exitCode = PocketTui.NOT_FOUND)
    public void testAddPocketBadName(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "The specified value [nonsense] doesn't match any known items.",
                "Defined items:",
                "[ 10] Rations (rations-1-day)");
        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "[  9] Rations (rations)");
    }

    @Test
    @Launch(value = { "-p=test-5e", "add", "rations-1-day" }, exitCode = ExitCode.USAGE)
    public void testAddPocketBadTo(LaunchResult result) {
        Util.assertConciseContentContains(result.getErrorStream(),
                "Must specify the item (rations-1-day) and the pocket the item should be added to (null)");
    }
}
