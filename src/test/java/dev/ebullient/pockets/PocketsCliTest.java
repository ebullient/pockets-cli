package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
class PocketsCliTest {

    @Test
    @Launch({})
    public void testBasicCommand(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "What have you got in your pockets?",
                "Create a new pocket",
                "Add an item to a pocket",
                "--verbose");
    }

    @Test
    public void testBasicCommandHelp(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("--help");
        LaunchResult result2 = launcher.launch("-h");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
    }

    @Test
    public void testVersionCommandHelp(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("--version");
        LaunchResult result2 = launcher.launch("-V");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
    }

    @Test
    @Launch({ "c", "--help" })
    public void testCreateCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Create a new pocket",
                "Type of pocket",
                "Pocket Attributes",
                "Parameters");
    }

    @Test
    @Launch({ "e", "--help" })
    public void testEditCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Edit the attributes of a pocket",
                "Edit attributes without confirmation",
                "Pocket Attributes");
    }

    @Test
    @Launch({ "d", "--help" })
    public void testDeleteCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Delete a pocket",
                "pocket to delete",
                "Parameters");
    }

    @Test
    @Launch({ "l", "--help" })
    public void testListCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains("List all pockets, or the contents of one pocket");
    }

    @Test
    @Launch({ "a", "--help" })
    public void testAddCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains("Add items to a pocket");
    }

    @Test
    @Launch({ "u", "--help" })
    public void testUpdateCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains("Update an item in a pocket");
    }

    @Test
    @Launch({ "r", "--help" })
    public void testRemoveCommandHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains("Remove an item from a pocket");
    }
}
