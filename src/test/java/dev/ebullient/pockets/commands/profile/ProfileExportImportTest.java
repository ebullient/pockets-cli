package dev.ebullient.pockets.commands.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import groovyjarjarpicocli.CommandLine.ExitCode;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class ProfileExportImportTest {
    public static final Path TARGET_DIR = Util.PROJECT_PATH.resolve("target/test/pockets");

    @Test
    @Launch({ "profile", "export", "--help" })
    public void testProfileExportHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Export profile settings.",
                "Usage: pockets profile export ");
    }

    @Test
    @Launch({ "profile", "import", "--help" })
    public void testProfileImportHelp(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "Import configuration settings into profile.",
                "Usage: pockets profile import ");
    }

    @Test
    @Launch(value = { "profile", "export" }, exitCode = ExitCode.USAGE)
    public void testProfileExportMissingFile(LaunchResult result) {
        Util.assertConciseContentContains(result.getErrorStream(),
                "Missing required option: '--file=<filePath>'",
                "Usage: pockets profile export ");
    }

    @Test
    @Launch(value = { "profile", "import" }, exitCode = ExitCode.USAGE)
    public void testProfileImportMissingFile(LaunchResult result) {
        Util.assertConciseContentContains(result.getErrorStream(),
                "Missing required option: '--file=<filePath>'",
                "Usage: pockets profile import ");
    }

    @Test
    public void testProfileUseYaml(QuarkusMainLauncher launcher) {
        Path output = TARGET_DIR.resolve("export.yaml");
        output.toFile().delete();

        LaunchResult result = launcher.launch("profile", "export", "-f", "--file", output.toAbsolutePath().toString());
        assertThat(result.exitCode()).isEqualTo(0);
        Util.assertConciseContentContains(result.getOutputStream(),
                "Settings have been exported from default.");
        assertThat(output).exists();
        assertThat(output).content().contains("\"dummy description\"");

        result = launcher.launch("profile", "import", "--file", output.toAbsolutePath().toString(), "copy");
        assertThat(result.exitCode()).isEqualTo(0);
        Util.assertConciseContentContains(result.getOutputStream(),
                "Settings have been imported into copy.");
        assertThat(output).exists();

        // If we write to existing file w/o -f, it should fail
        result = launcher.launch("profile", "export", "--file", output.toAbsolutePath().toString());
        assertThat(result.exitCode()).isEqualTo(2);
    }

    @Test
    public void testProfileUseJson(QuarkusMainLauncher launcher) throws IOException {
        Path output = TARGET_DIR.resolve("export.json");
        output.toFile().delete();

        LaunchResult result = launcher.launch("profile", "export", "-f", "--file", output.toAbsolutePath().toString(),
                "Test 5e");
        assertThat(result.exitCode()).isEqualTo(0);
        Util.assertConciseContentContains(result.getOutputStream(),
                "Settings have been exported from test-5e.");
        assertThat(output).exists();
        assertThat(output).content().contains("\"dnd5e\"");

        result = launcher.launch("profile", "import", "--file", output.toAbsolutePath().toString(), "copy");
        assertThat(result.exitCode()).isEqualTo(0);
        Util.assertConciseContentContains(result.getOutputStream(),
                "Settings have been imported into copy.");
    }

    @Test
    public void testProfileChangeFlavor(QuarkusMainLauncher launcher) throws IOException {
        Path output = TARGET_DIR.resolve("output.json");

        Files.write(output, List.of("{\"id\":\"test-5e\",\"preset\":\"pf2e\",\"currency\":[],\"pocketRef\":[]}"));

        // Try over-writing the original with a different preset: should fail.
        LaunchResult result = launcher.launch("profile", "import", "--file", output.toAbsolutePath().toString(), "test-5e");

        assertThat(result.exitCode()).isEqualTo(ExitCode.USAGE);
        Util.assertConciseContentContains(result.getErrorStream(),
                "Unable to import settings. You can not change the preset associated with a profile once it has been set.");
    }
}
