package dev.ebullient.pockets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.inject.Inject;
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

    @Inject
    CommonIO io;

    @Option(names = { "-f", "--force" }, description = "Edit attributes without confirmation or prompting", required = false)
    boolean force = false;

    @ArgGroup(exclusive = false, heading = "%nPocket Attributes:%n")
    PocketAttributes attrs = new PocketAttributes();

    @Parameters(index = "0", description = "Name or id of pocket to delete.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Optional<Long> id = CommonIO.toLong(nameOrId, false);
        Term.debugf("Parameters: %s, %s, %s", nameOrId, id, attrs);

        Pocket pocket = id.isPresent()
                ? io.selectPocketById(id.get())
                : io.selectPocketByName(nameOrId);

        Term.debugf("Pocket to edit: %s", pocket);

        if (pocket == null) {
            return ExitCode.USAGE;
        }

        ParseResult pr = spec.commandLine().getParseResult();
        boolean saveIt = force;
        Previous previous = new Previous(pocket);

        if (Term.isVerbose()) {
            Term.outPrintf("%n%-2s %s [%d] has the following attributes:%n", io.getPocketEmoji(pocket), pocket.name, pocket.id);
            io.describe(pocket);
        }
        if (Term.canPrompt() && !force) {
            promptForUpdates(pocket, pr);
        }
        if (attrs.max_weight.isPresent()) {
            pocket.max_weight = attrs.max_weight.get();
        }
        if (attrs.max_volume.isPresent()) {
            pocket.max_volume = attrs.max_volume.get();
        }
        if (attrs.weight.isPresent()) {
            pocket.weight = attrs.weight.get();
        }
        if (pr.hasMatchedOption("--magic")) {
            pocket.extradimensional = attrs.magic;
        }

        if (previous.isUnchanged(pocket)) {
            Term.outPrintf("%n🔶 %s [%d] was not updated (no changes).%n", pocket.name, pocket.id);
            return ExitCode.OK;
        }

        if (Term.isVerbose()) {
            Term.outPrintf("%n✨ %s [%d] now has the following attributes:%n", pocket.name, pocket.id);
            io.describe(pocket);
        }
        if (Term.canPrompt() && !force) {
            String line = Term.prompt("Save your changes (y|N)? ");
            saveIt = CommonIO.yesOrTrue(line, false);
        }
        if (saveIt) {
            pocket.persistAndFlush();
            io.checkFieldWidths(pocket);
            Term.outPrintf("%n✅ %s [%d] has been updated.%n", pocket.name, pocket.id);
        } else if (!force) {
            Term.outPrintf("%n🔶 %s [%d] was not updated (requires confirmation or use --force).%n", pocket.name, pocket.id);
            return ExitCode.USAGE;
        }
        return ExitCode.OK;
    }

    void promptForUpdates(Pocket pocket, ParseResult pr) throws IOException {
        String line = null;
        Term.outPrintln("Press enter to keep the previous value.");

        if (attrs.weight.isEmpty()) {
            line = Term.prompt("Weight of this pocket (when empty) in pounds [" + pocket.weight + "]: ");
            pocket.weight = CommonIO.toDoubleOrDefault(line, pocket.weight);
        }
        if (attrs.max_weight.isEmpty()) {
            line = Term.prompt("Enter the maximum weight of this pocket in pounds [" + pocket.max_weight + "]: ");
            pocket.max_weight = CommonIO.toDoubleOrDefault(line, pocket.max_weight);
        }
        if (attrs.max_volume.isEmpty()) {
            line = Term.prompt("Enter the maximum volume of this pocket in cubic feet [" + pocket.max_volume + "]: ");
            pocket.max_volume = CommonIO.toDoubleOrDefault(line, pocket.max_volume);
        }
        if (!pr.hasMatchedOption("--magic")) {
            String previous = pocket.extradimensional ? "Y" : "N";
            Term.outPrintln("Magic pockets always weigh the same, regardless of their contents.");
            line = Term.prompt("Is this a magic pocket [" + previous + "]? ");
            pocket.extradimensional = CommonIO.yesOrTrue(line, pocket.extradimensional);
        }
    }

    class Previous {
        String name;
        double max_volume; // in cubic ft
        double max_weight; // in lbs
        double weight; // weight of the pocket itself
        boolean magic; // magic pockets always weigh the same

        Previous(Pocket pocket) {
            this.name = pocket.name;
            this.max_volume = pocket.max_volume;
            this.max_weight = pocket.max_weight;
            this.weight = pocket.weight;
            this.magic = pocket.extradimensional;
        }

        public boolean isUnchanged(Pocket pocket) {
            return this.name.equals(pocket.name)
                    && this.max_volume == pocket.max_volume
                    && this.max_weight == pocket.max_weight
                    && this.weight == pocket.weight
                    && this.magic == pocket.extradimensional;
        }
    }
}
