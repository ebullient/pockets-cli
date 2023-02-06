package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketsCliTest {

    @Test
    public void testHelpCommand(QuarkusMainLauncher launcher) {
        LaunchResult r1 = launcher.launch("--help");
        assertThat(r1.exitCode()).isEqualTo(0);

        LaunchResult r2 = launcher.launch("-h");
        assertThat(r2.exitCode()).isEqualTo(0);

        assertThat(Util.outputWithoutLogs(r1)).isEqualTo(Util.outputWithoutLogs(r2));
        assertThat(r1.getOutput()).contains("Usage: pockets");
    }
}
