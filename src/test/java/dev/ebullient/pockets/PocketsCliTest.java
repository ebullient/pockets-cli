package dev.ebullient.pockets;

import static dev.ebullient.pockets.Util.PROJECT_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
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

        assertThat(Util.conciseOutput(r1)).isEqualTo(Util.conciseOutput(r2));
        assertThat(r1.getOutput()).contains("Usage: pockets");
    }

    @Test
    @Launch({ "add", "--types" })
    public void test5eTypes(LaunchResult result) {
        assertThat(Util.conciseOutput(result.getOutput())).contains(
                Util.conciseOutput("portable-hole Portable Hole"),
                Util.conciseOutput("boots-of-striding-and-springing Boots of Striding and Springing"));
    }

    @Test
    @Launch({ "add", "--types", "-p", "test-pf2e" })
    public void testPf2eTypes(LaunchResult result) {
        assertThat(Util.conciseOutput(result.getOutput())).contains(
                Util.conciseOutput("pathfinders-pouch Pathfinder's Pouch"),
                Util.conciseOutput("adamantine-chunk  Adamantine (chunk)"));
    }

    @Test
    public void testGenerateItems(QuarkusMainLauncher launcher) {
        final Path SOURCES = PROJECT_PATH.resolve("sources");
        final Path TOOLS_5e = SOURCES.resolve("5etools-mirror-1.github.io/data/");
        final Path TOOLS_pf2e = SOURCES.resolve("Pf2eTools/data/");
        final Path TARGET = PROJECT_PATH.resolve("target/pockets/gen");

        if (TOOLS_5e.toFile().exists()) {
            LaunchResult result = launcher.launch("profile", "presets", "-s DMG,PHB",
                    "-o", TARGET.resolve("5e").toString(),
                    TOOLS_5e.resolve("items-base.json").toString(),
                    TOOLS_5e.resolve("items.json").toString());
            assertThat(result.exitCode()).isEqualTo(0);
        }

        if (TOOLS_pf2e.toFile().exists()) {
            LaunchResult result = launcher.launch("profile", "presets", "-s ALL",
                    "-o", TARGET.resolve("p2fe").toString(),
                    TOOLS_pf2e.resolve("items/baseitems.json").toString(),
                    TOOLS_pf2e.resolve("items/items-crb.json").toString());
            assertThat(result.exitCode()).isEqualTo(0);
        }
    }
}
