package dev.ebullient.pockets.commands.profile;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ProfileRemoveTest {

    @Test
    @Launch({ "profile", "remove", "--help" })
    public void testProfileRemoveHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Remove a config profile",
                "Usage: pockets profile remove");
    }

    @Test
    @Launch({ "profile", "remove", "test-5e" }) // remove w/ slugified form of name
    public void testProfileRemoveCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Profile test-5e has been removed.",
                "Profiles defined:",
                "ID Description",
                "default dummy description");
        assertThat(result.getOutputStream()).noneMatch(s -> "test-5e".equals(s.trim()));
    }

    @Test
    @Launch({ "profile", "remove" })
    public void testProfileRemoveDefaultCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Profile default has been reset.",
                "Profiles defined:",
                "ID Description",
                "default ");
        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                " dummy description");
    }
}
