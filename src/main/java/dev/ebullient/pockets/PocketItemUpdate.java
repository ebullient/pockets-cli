package dev.ebullient.pockets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(name = "u", aliases = { "update" }, header = "Update an item in a pocket", description = Constants.ADD_DESCRIPTION)
public class PocketItemUpdate implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Inject
    CommonIO io;

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    @Option(names = { "-f", "--force" }, description = "Edit attributes without confirmation or prompting", required = false)
    boolean force = false;

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    PocketItemAttributes attrs = new PocketItemAttributes();

    String nameOrId;

    @Parameters(index = "1", description = "Name or id of item to delete from pocket.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Optional<Long> id = CommonIO.toLong(nameOrId, false);
        Term.debugf("Parameters: %s, %s -> %s", pocketId, nameOrId, id);

        Pocket pocket = io.selectPocketById(pocketId);
        if (pocket == null) {
            return ExitCode.USAGE;
        }

        PocketItem item = id.isPresent()
                ? io.selectPocketItemById(pocket, id.get())
                : io.selectPocketItemByName(pocket, nameOrId);

        Term.debugf("Pocket item to edit: %s", item);

        if (item == null) {
            return ExitCode.USAGE;
        }
        Previous previous = new Previous(item);
        ParseResult pr = spec.commandLine().getParseResult();

        boolean saveIt = force;
        int result = ExitCode.OK;
        if (Term.canPrompt() && !force) {
            if (Term.isVerbose()) {
                Term.outPrintf("%n%s [%d] has the following attributes:%n", item.name, item.id);
                io.describe(item);
            }
            promptForUpdates(item, pr);
        }
        if (attrs.value.isPresent()) {
            item.gpValue = CommonIO.gpValueOrDefault(attrs.value.get(), item.gpValue);
        }
        if (attrs.weight.isPresent()) {
            item.weight = attrs.weight.get();
        }
        if (pr.hasMatchedOption("--quantity")) {
            item.quantity = attrs.quantity;
        }
        if (previous.isUnchanged(item)) {
            Term.outPrintf("%n🔶 %s [%d] was not updated. There are no changes.%n", item.name, item.id);
            return ExitCode.OK;
        }

        if (Term.canPrompt() && !force) {
            if (Term.isVerbose()) {
                Term.outPrintf("%n✨ %s [%d] will be updated with the following attributes:%n", item.name, item.id);
                io.describe(item);
            }
            String line = Term.prompt("Save your changes (y|N)? ");
            saveIt = CommonIO.yesOrTrue(line, false);
        }
        if (saveIt) {
            item.persistAndFlush();
            io.checkFieldWidths(item);

            Term.outPrintf("%n✅ %s [%d] has been updated.%n", item.name, item.id);
        } else {
            Term.outPrintf("%n🔶 %s [%d] was not updated (requires confirmation or use --force).%n", item.name, item.id);
            result = ExitCode.USAGE;
        }

        if (Term.isVerbose()) {
            io.listPocketContents(pocket);
        }
        return result;
    }

    void promptForUpdates(PocketItem item, ParseResult pr) throws IOException {
        String line = null;
        Term.outPrintln("Press enter to keep the previous value.");

        if (!pr.hasMatchedOption("--quantity")) {
            line = Term.prompt("How many (" + item.quantity + "): ");
            item.quantity = (int) CommonIO.toLongOrDefault(line, item.quantity);
        }

        Term.outPrintln("For the following prompts, use a space to remove the previous value.");
        if (attrs.value.isEmpty()) {
            line = Term.prompt("Enter the value of a single item (" + item.gpValue + "gp): ");
            if (line.length() > 0 && line.isBlank()) {
                item.gpValue = null;
            } else {
                item.gpValue = CommonIO.gpValueOrDefault(line, item.gpValue);
            }
        }
        if (attrs.weight.isEmpty()) {
            line = Term.prompt("Enter the weight of this item in pounds (" + item.weight + "): ");
            if (line.length() > 0 && line.isBlank()) {
                item.weight = null;
            } else {
                item.weight = CommonIO.toDoubleOrDefault(line, item.weight);
            }
        }
    }

    class Previous {
        public String name;
        public int quantity;
        public Double weight; // weight in lbs
        public Double value; // value in gp

        Previous(PocketItem item) {
            this.quantity = item.quantity;
            this.name = item.name;
            this.value = item.gpValue;
            this.weight = item.weight;
        }

        public boolean isUnchanged(PocketItem item) {
            return this.name.equals(item.name)
                    && this.quantity == item.quantity
                    && this.value == item.gpValue
                    && this.weight == item.weight;
        }
    }
}
