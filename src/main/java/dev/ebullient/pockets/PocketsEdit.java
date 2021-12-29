package dev.ebullient.pockets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(name = "e", aliases = { "edit" }, header = "Edit the attributes of a pocket")
public class PocketsEdit implements Callable<Integer> {
    String nameOrId;

    @Spec
    private CommandSpec spec;

    @Option(names = { "-f", "--force" }, description = "Edit attributes without confirmation", required = false)
    boolean force = false;

    @ArgGroup(exclusive = false, heading = "%nPocket Attributes:%n")
    CommonIO.PocketAttributes attrs = new CommonIO.PocketAttributes();

    @Parameters(index = "0", description = "Name or id of pocket to delete.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Optional<Long> id = CommonIO.toLong(nameOrId);
        Term.debugf("Parameters: %s, %s, %s", nameOrId, id, attrs);

        Pocket pocket = id.isPresent()
                ? CommonIO.selectPocketById(id.get())
                : CommonIO.selectPocketByName(nameOrId);

        Term.debugf("Pocket to edit: %s", pocket);

        if (pocket == null) {
            return ExitCode.USAGE;
        } else {
            ParseResult pr = spec.commandLine().getParseResult();
            boolean saveIt = force;

            if (Term.isVerbose()) {
                Term.outPrintf("%n%-2s %s [%d] has the following attributes:%n", pocket.type.icon(), pocket.name, pocket.id);
                CommonIO.describe(pocket);
            }

            if (Term.canPrompt()) {
                promptForUpdates(pocket, pr);
            }

            if (attrs.max_capacity.isPresent()) {
                pocket.max_capacity = attrs.max_capacity.get();
            }
            if (attrs.max_volume.isPresent()) {
                pocket.max_volume = attrs.max_volume.get();
            }
            if (attrs.weight.isPresent()) {
                pocket.weight = attrs.weight.get();
            }
            if (pr.hasMatchedOption("--magic")) {
                pocket.magic = attrs.magic;
            }

            if (Term.isVerbose()) {
                Term.outPrintf("%n✨ %s [%d] now has the following attributes:%n", pocket.name, pocket.id);
                CommonIO.describe(pocket);
            }

            if (Term.canPrompt() && !force) {
                String line = Term.prompt("Save your changes (y|N)? ");
                saveIt = CommonIO.yesOrTrue(line, false);
            }

            if (saveIt) {
                pocket.persistAndFlush();
                Term.outPrintf("%n✅ %s [%d] has been updated.%n", pocket.name, pocket.id);
            }
        }

        return ExitCode.OK;
    }

    void promptForUpdates(Pocket pocket, ParseResult pr) throws IOException {
        String line = null;
        if (attrs.max_capacity.isEmpty()) {
            line = Term.prompt("Enter the maximum capacity of this pocket in pounds (" + pocket.max_capacity + "): ");
            pocket.max_capacity = CommonIO.toDoubleOrDefault(line, pocket.max_capacity);
        }
        if (attrs.max_volume.isEmpty()) {
            line = Term.prompt("Enter the maximum volume of this pocket in cubic feet (" + pocket.max_volume + "): ");
            pocket.max_volume = CommonIO.toDoubleOrDefault(line, pocket.max_volume);
        }
        if (attrs.weight.isEmpty()) {
            line = Term.prompt("Weight of the pocket itself in pounds (" + pocket.weight + "): ");
            pocket.weight = CommonIO.toDoubleOrDefault(line, pocket.weight);
        }
        if (!pr.hasMatchedOption("--magic")) {
            String previous = pocket.magic ? "Y" : "N";
            Term.outPrintln("Magic pockets always weigh the same, regardless of their contents.");
            line = Term.prompt("Is this a magic pocket (" + previous + ")? ");
            pocket.magic = CommonIO.yesOrTrue(line, pocket.magic);
        }
    }
}
