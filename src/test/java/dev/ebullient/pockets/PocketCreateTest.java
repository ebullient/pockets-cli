package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class PocketCreateTest {

    @Test
    @Launch({"c", "basket",  "--debug"})
    public void testCreateCommand() {
    }

    @Test
    @Launch({"c", "--types"})
    public void testCreateTypesCommand() {
    }

    @Test
    @Launch({"c", "--help"})
    public void testCreateHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "Create a new pocket",
            "Usage: pockets c ");
    }
}
