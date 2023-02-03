package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import picocli.CommandLine.ExitCode;

@QuarkusMainTest
public class CoinsTest {

    @Test
    @Launch(value = { "$", "1" }, exitCode = ExitCode.USAGE)
    public void testCoinsMissingArguments(LaunchResult result) {
        assertThat(result.getErrorOutput()).contains(
                "Missing required parameters: '<operation>', '<values>'",
                "Usage: pockets $ ");
    }

    @Test
    @Launch(value = { "$", "1", "-", "10pp" }, exitCode = PocketTui.INSUFFICIENT_FUNDS)
    public void testInsufficientFunds(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "You are trying to remove more coins",
                "No changes have been saved.");
    }

    @Test
    @Launch({ "$", "1", "+", "1sp" })
    public void testCoinsAdd(LaunchResult result) {
        assertThat(Util.noWhitespace(result.getOutput()))
                .contains(
                        Util.noWhitespace("Coins [1] contains:"),
                        Util.noWhitespace("] (  0)  Platinum (pp)  0.02    10.0   "),
                        Util.noWhitespace("] ( 50)  Gold (gp)      0.02     1.0   "),
                        Util.noWhitespace("] (  2)  Silver (sp)    0.02     0.1   "));
    }

    @Test
    @Launch({ "$", "--help" })
    public void testCoinsHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Do you have change in your pocket?",
                "Usage: pockets $ ");
    }

}
