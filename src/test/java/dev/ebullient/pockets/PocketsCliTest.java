package dev.ebullient.pockets;

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
    public void testBasicCommandVersion(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("--version");
        LaunchResult result2 = launcher.launch("-V");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
    }
}
