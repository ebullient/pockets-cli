package dev.ebullient.pockets.commands.profile;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ProfileCommandTest {

    @Test
    @Launch({ "profile", "--help" })
    public void testProfileHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Add, update, or delete a config profile.",
                "Usage: pockets profile");
    }

    @Test
    @Launch({ "profile" })
    public void testProfileListCommand(LaunchResult result) {
        assertThat(Util.conciseOutput(result.getOutput())).contains(
                Util.conciseOutput("Profiles defined:"),
                Util.conciseOutput("ID Description"),
                Util.conciseOutput("default "));
    }

    @Test
    @Launch({ "profile", "default" })
    public void testProfileListDefaultCommand(LaunchResult result) {
        assertThat(Util.conciseOutput(result.getOutput())).contains(
                Util.conciseOutput("Profile default uses preset dnd5e."));
    }
}
