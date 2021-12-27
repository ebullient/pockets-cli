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
    public void testCreateCommandHelp(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("create", "--help");
        LaunchResult result2 = launcher.launch("create", "-h");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
    }

    @Test
    public void testAddCommandHelp(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("add", "--help");
        LaunchResult result2 = launcher.launch("add", "-h");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
    }

    @Test
    public void testListCommandHelp(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("list", "--help");
        LaunchResult result2 = launcher.launch("list", "-h");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
        assertThat(result.getOutput()).contains("List all pockets, or the contents of one pocket");
    }

}
