package dev.ebullient.pockets.index;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
public class Import5etoolsTest {
    @Test
    @Launch({ "import", "5etools", "-o", "target", "src/test/resources/5etools_srd.json", "--brief" })
    public void testConvertJson(LaunchResult result) {
        Util.assertConciseContentContains(result.getOutputStream(),
                "5etoolsIndex.json",
                "Done.");
    }
}
