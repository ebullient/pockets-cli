package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
@TestTransaction
public class PocketsAddTest {

    @Test
    public void testAdd(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("add", "2", "Jeweled Eyepatch");
        Assertions.assertEquals(0, result.exitCode());
        assertThat(result.getOutput()).contains("Jeweled Eyepatch");
    }

    @Test
    public void testAddUnknownPocket(QuarkusMainLauncher launcher) {
        // Aliases should emit the same output
        LaunchResult result = launcher.launch("add", "12", "Jeweled Eyepatch");
        Assertions.assertEquals(0, result.exitCode());
        assertThat(result.getOutput()).contains(
                "Id 12 doesn't match any of your pockets.",
                "Your pockets:");
    }
}
