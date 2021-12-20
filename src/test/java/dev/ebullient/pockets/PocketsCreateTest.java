package dev.ebullient.pockets;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsCreateTest {

    @Test
    public void testBasicCommandHelp(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("create", "--help");
        LaunchResult result2 = launcher.launch("create", "-h");
        Assertions.assertEquals(0, result.exitCode());
        Assertions.assertEquals(Util.outputWithoutLogs(result), Util.outputWithoutLogs(result2));
    }
}
