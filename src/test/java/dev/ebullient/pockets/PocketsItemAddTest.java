package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@QuarkusMainTest
@TestTransaction
public class PocketsItemAddTest {

    @Test
    @Launch({ "add", "2", "Jeweled Eyepatch", "--brief" })
    public void testAdd(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(1) Jeweled Eyepatch [6] added to Backpack [2]");

        assertThat(result.getOutput()).doesNotContain(
                "Backpack [2] contains", // verbose
                "[   5] ( 10)  Rations", // verbose
                "This backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch({ "add", "2", "-v", "30gp", "Jeweled Eyepatch" })
    public void testAddVerbose(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "(1) Jeweled Eyepatch [6] added to Backpack [2]",
                "Backpack [2] contains", // verbose
                "[   6] (  1)  Jeweled Eyepatch                                        -    30.0",
                "[   5] ( 10)  Rations", // verbose
                "This backpack weighs 5.0 pounds when empty."); // verbose
    }

    @Test
    @Launch(value = { "add", "12", "Jeweled Eyepatch" }, exitCode = 2)
    public void testAddUnknownPocket(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "The specified value [12] doesn't match any of your pockets.",
                "Your pockets:");
    }
}
