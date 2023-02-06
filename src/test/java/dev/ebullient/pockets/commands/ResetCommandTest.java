package dev.ebullient.pockets.commands;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ResetCommandTest {

    @Test
    @Launch({ "reset", "--help" })
    public void testProfileHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Reset everything.",
                "Usage: pockets reset");
    }

    @Test
    @Launch({ "reset" })
    public void testProfileResetCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "All data has been reset.",
                "Profiles defined:",
                "ID  Description",
                "default ");
        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "test-dnd5e ",
                "test-pf2e ",
                "dummy description");
    }
}
