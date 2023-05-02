package dev.ebullient.pockets.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Util;
import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;
import picocli.CommandLine.ExitCode;

@QuarkusMainTest
public class CreateItemCommandTest extends CommandTestBase {

    @Test
    @Launch({ "create", "--help" })
    public void testCreateHelp(LaunchResult result) {
        assertThat(result.getOutput()).contains(
                "Create an item or pocket.",
                "Usage: pockets create ");
        assertThat(result.getErrorOutput()).isBlank();
    }

    @Test
    @Launch({ "-p", "test-5e", "create",
            "--type=arrow",
            "--no-trade",
            "--magic",
            "--notes=Talking arrow. Loves Shakespeare. Annoying.",
            "Arrow", "of", "Dedona" })
    public void testCreatedItem(LaunchResult result) throws IOException {
        assertThat(testOutputPath).exists();
        assertThat(result.getErrorOutput()).isBlank();

        Util.assertConciseContentContains(result.getOutputStream(),
                "Created [ 17] Arrow of Dedona (arrow-of-dedona)",
                "Weight       :  0.05 pounds",
                "Value        : 5cp",
                "Magical      : true",
                "This item is not contained in any known");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "in the following pockets");

        Util.assertFileContent(testOutputPath, "test-5e.events.jsonl",
                "Create Arrow of Dedona",
                "\"itemId\":\"arrow-of-dedona\"",
                "\"itemDetails\":{\"refId\":\"arrow\",\"notes\":\"Talking arrow. Loves Shakespeare. Annoying.\"",
                "\"weight\":0.05,\"baseUnitValue\":5.0,\"tradable\":false,\"magical\":true");

        Util.assertFileContent(testOutputPath, "test-5e/eventLog.md",
                "cssclass: pockets,events",
                "<th>Date / Time</th>",
                "<td>Create Arrow of Dedona<br />Created Arrow of Dedona; item; based on 'arrow'; 0.05 pounds; worth 5cp; Talking arrow. Loves Shakespeare. Annoying.</td>",
                "<td>arrow-of-dedona</td>");

        Util.assertFileContent(testOutputPath, "test-5e/inventory.md",
                "cssclass: pockets,inventory",
                "### Other known items",
                "| Arrow of Dedona | \uD83E\uDE84 | 0.05 pounds | 5cp |");
    }

    @Test
    @Launch({ "-p=test-5e", "create",
            "--pocket",
            "--type=backpack",
            "--from=start",
            "Sarah's backpack" })
    public void testCreateAddPocket(LaunchResult result) throws IOException {
        assertThat(testOutputPath).exists();
        assertThat(result.getErrorOutput()).isBlank();

        Util.assertConciseContentContains(result.getOutputStream(),
                "Created [ 19] Sarah's backpack (sarahs-backpack)",
                "Item Weight  : 5.0 pounds (not including contents)",
                "Item Value   : 2gp (not including contents)",
                "Tradable     : true (partial value)",
                "Max Weight   : 30.0 pounds; 1 cubic foot");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "Bottomless   :",
                "in the following pockets");

        Util.assertFileContent(testOutputPath, "test-5e.events.jsonl",
                "Create Sarah's backpack",
                "\"itemType\":\"POCKET\",\"itemId\":\"sarahs-backpack\"",
                "\"slug\":\"sarahs-backpack\",\"name\":\"Sarah's backpack\",\"emoji\":\"\uD83C\uDF92\"",
                "\"refId\":\"backpack\",\"weight\":5.0,\"baseUnitValue\":200.0");

        Util.assertFileContent(testOutputPath, "test-5e/eventLog.md",
                "cssclass: pockets,events",
                "<th>Date / Time</th>",
                "<td>Create Sarah's backpack<br />Created Sarah's backpack; pocket; based on 'backpack'; 5.0 pounds; worth 2gp; tradable; can hold at most 30.0 pounds or 1 cubic foot</td>",
                "<td>sarahs-backpack</td>");

        Util.assertFileContent(testOutputPath, "test-5e/inventory.md",
                "cssclass: pockets,inventory",
                "| \uD83C\uDF92 | [Sarah's backpack](pocket-sarahs-backpack.md) |  | 30.0 pounds; 1 cubic foot |");

        Util.assertFileContent(testOutputPath, "test-5e/pocket-sarahs-backpack.md",
                "cssclass: pockets,pocket",
                "aliases: [\"Sarah's backpack\"]");
    }

    @Test
    @Launch({ "-p", "test-5e", "create",
            "--pocket",
            "--type=npc",
            "--bottomless",
            "--notes", "Shop in Sunnyvale. Specializes in building supplies.",
            "Little Pig Emporium" })
    public void testCreateBottomlessPocket(LaunchResult result) throws IOException {
        assertThat(testOutputPath).exists();
        assertThat(result.getErrorOutput()).isBlank();

        Util.assertConciseContentContains(result.getOutputStream(),
                "Created [ 18] Little Pig Emporium (little-pig-emporium)",
                "This is a bottomless pocket.");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "Max Weight   :",
                "Tradable     :");

        Util.assertFileContent(testOutputPath, "test-5e.events.jsonl",
                "\"memo\":\"Create Little Pig Emporium\"",
                "\"itemType\":\"POCKET\",\"itemId\":\"little-pig-emporium\"",
                "\"slug\":\"little-pig-emporium\",\"name\":\"Little Pig Emporium\",\"emoji\":\"\uD83D\uDC64\"",
                "\"refId\":\"npc\",\"notes\":\"Shop in Sunnyvale. Specializes in building supplies.\"",
                "\"tradable\":false,\"fullValueTrade\":false,\"bottomless\":true");

        Util.assertFileContent(testOutputPath, "test-5e/eventLog.md",
                "cssclass: pockets,events",
                "<th>Date / Time</th>",
                "<td>Create Little Pig Emporium<br />Created Little Pig Emporium; pocket; based on 'npc'; Shop in Sunnyvale. Specializes in building supplies.</td>",
                "<td>little-pig-emporium</td>");

        Util.assertFileContent(testOutputPath, "test-5e/inventory.md",
                "cssclass: pockets,inventory",
                "| \uD83D\uDC64∞ | [Little Pig Emporium](pocket-little-pig-emporium.md) |  | N/A |");

        Util.assertFileContent(testOutputPath, "test-5e/pocket-little-pig-emporium.md",
                "cssclass: pockets,pocket",
                "aliases: [\"Little Pig Emporium\"]",
                "This is a bottomless pocket. ",
                "Shop in Sunnyvale. Specializes in building supplies.");
    }

    @Test
    @Launch({ "-p=test-5e", "create",
            "--pocket",
            "--type=pc",
            "--max-weight=225",
            "Brunhilde" })
    public void testCreateConstrainedPocket(LaunchResult result) throws IOException {
        assertThat(testOutputPath).exists();
        assertThat(result.getErrorOutput()).isBlank();

        Util.assertConciseContentContains(result.getOutputStream(),
                "Created [ 18] Brunhilde (brunhilde)",
                "Max Weight   : 225.0 pounds");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "Bottomless :",
                "Tradable :",
                "in the following pockets");

        Util.assertFileContent(testOutputPath, "test-5e.events.jsonl",
                "\"memo\":\"Create Brunhilde\"",
                "\"itemType\":\"POCKET\",\"itemId\":\"brunhilde\",\"created\":{\"slug\":\"brunhilde\",\"name\":\"Brunhilde\"",
                "\"emoji\":\"\uD83D\uDE07\",\"pocketDetails\":{\"refId\":\"pc\",",
                "\"tradable\":false,\"fullValueTrade\":false,\"max_weight\":225.0");

        Util.assertFileContent(testOutputPath, "test-5e/eventLog.md",
                "cssclass: pockets,events",
                "<th>Date / Time</th>",
                "<td>Create Brunhilde<br />Created Brunhilde; pocket; based on 'pc'; can hold at most 225.0 pounds; A character's Strength score multiplied by 15 is",
                "<td>brunhilde</td>");

        Util.assertFileContent(testOutputPath, "test-5e/inventory.md",
                "cssclass: pockets,inventory",
                "| \uD83D\uDE07 | [Brunhilde](pocket-brunhilde.md) |  | 225.0 pounds |");

        Util.assertFileContent(testOutputPath, "test-5e/pocket-brunhilde.md",
                "cssclass: pockets,pocket",
                "aliases: [\"Brunhilde\"]",
                "> - **Reference item** pc",
                "A character's Strength score multiplied by 15 is the weight (in pounds) that they can carry.");
    }

    @Test
    @Launch({ "-p=test-5e", "create",
            "--value=50gp",
            "--weight=1",
            "--from=loot",
            "--to=backpack",
            "--id=tiny-ivory-crocodile",
            "--notes", "Found in insane tomb",
            "Carved ivory crocodile" })
    public void testCreateAddItem(LaunchResult result) throws IOException {
        assertThat(testOutputPath).exists();
        assertThat(result.getErrorOutput()).isBlank();

        Util.assertConciseContentContains(result.getOutputStream(),
                "Created [ 17] Carved ivory crocodile (tiny-ivory-crocodile)",
                "Found in insane tomb",
                "Weight       : 1 pound",
                "Value        : 5pp",
                "Tradable     : true (full value)",
                "[ 17] Carved ivory crocodile (tiny-ivory-crocodile) is in the following pockets",
                "(   1)     \uD83E\uDD2A  [  7] Backpack (backpack)");

        Util.assertConciseContentDoesNotContain(result.getOutputStream(),
                "Max Weight   :");

        Util.assertFileContent(testOutputPath, "test-5e.events.jsonl",
                "\"memo\":\"Create Carved ivory crocodile\"",
                "\"itemType\":\"ITEM\",\"itemId\":\"tiny-ivory-crocodile\"",
                "\"slug\":\"tiny-ivory-crocodile\",\"name\":\"Carved ivory crocodile\"",
                "\"notes\":\"Found in insane tomb\",\"weight\":1.0,\"baseUnitValue\":5000.0,\"tradable\":true,\"fullValueTrade\":true",
                "{\"type\":\"ADD\",\"itemType\":\"ITEM\",\"pocketId\":\"backpack\",\"itemId\":\"tiny-ivory-crocodile\",\"quantity\":1}");

        Util.assertFileContent(testOutputPath, "test-5e/eventLog.md",
                "cssclass: pockets,events",
                "<th>Date / Time</th>",
                "<td>Create Carved ivory crocodile<br />Created Carved ivory crocodile; item; 1 pound; worth 5pp; tradable; Found in insane tomb</td>",
                "<td>Create Carved ivory crocodile</td>",
                "<td>tiny-ivory-crocodile</td>");

        Util.assertFileContent(testOutputPath, "test-5e/inventory.md",
                "cssclass: pockets,inventory",
                "| 1 | Carved ivory crocodile | \uD83D\uDCB0 | 1 pound | 5pp | [Backpack](pocket-backpack.md) (1) |");

        Util.assertFileContent(testOutputPath, "test-5e/pocket-backpack.md",
                "cssclass: pockets,pocket",
                "aliases: [\"Backpack\"]",
                "> - **Max Weight** 30.0 pounds; 1 cubic foot",
                "- \uD83D\uDCB0 **Current value**: 5pp, 2gp",
                "- ⚖️ **Currently carrying**: 21.0 pounds");
    }

    @Test
    @Launch(value = { "-p=test-5e", "create",
            "--pocket",
            "--notes=Shop in Sunnyvale. Specializes in building supplies." }, exitCode = ExitCode.USAGE)
    public void testCreatePocketNoName(LaunchResult result) {
        Util.assertConciseContentContains(result.getErrorStream(),
                "Must specify a name (null), id (null), or item reference(null)");
    }
}
