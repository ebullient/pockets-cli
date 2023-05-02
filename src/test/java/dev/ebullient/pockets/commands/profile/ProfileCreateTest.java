package dev.ebullient.pockets.commands.profile;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ProfileCreateTest {

    @Test
    @Launch({ "profile", "create", "--help" })
    public void testProfileCreateHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Create a config profile",
                "Usage: pockets profile create");
    }

    @Test
    @Launch({ "profile", "create", "test" })
    public void testProfileCreateCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Profile test created.",
                "Profile test uses preset dnd5e");
    }

    @Test
    @Launch({ "profile", "create", "--preset", "pf2e", "--desc", "Test with preset", "test-preset" })
    public void testProfileCreateWithPresetCommand(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Profile test-preset uses preset pf2e",
                "Test with preset");
    }

}
