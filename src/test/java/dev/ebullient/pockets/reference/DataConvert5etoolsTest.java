package dev.ebullient.pockets.reference;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

//@Disabled
@QuarkusMainTest
public class DataConvert5etoolsTest {
    @Test
    @Launch({ "5etools", "-o", "target", "src/test/resources/5etools_srd.json" })
    public void testConvertJson(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "index.json",
                "Done.");
    }
}
