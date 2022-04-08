package dev.ebullient.pockets;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusMainTest
public class ItemRemoveTest {
    @Test
    @Launch({"a", "1", "basket",  "--debug"})
    public void testCreateCommand() {
    }

    @Test
    @Launch({"a", "--help"})
    public void testCreateHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
            "Add an item to a pocket",
            "Usage: pockets a ");
    }
}
