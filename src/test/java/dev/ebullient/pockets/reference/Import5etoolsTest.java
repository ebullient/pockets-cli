package dev.ebullient.pockets.reference;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class Import5etoolsTest {
    @Test
    @Launch({ "import", "5etools", "-o", "target", "src/test/resources/5etools_srd.json" })
    public void testConvertJson(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "5etoolsIndex.json",
                "Done.");
    }
}
