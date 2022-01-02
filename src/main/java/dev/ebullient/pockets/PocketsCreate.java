package dev.ebullient.pockets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.CommonIO.PocketAttributes;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketType;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

@Command(name = "c", aliases = { "create" }, header = "Create a new pocket")
public class PocketsCreate implements Callable<Integer> {
    @Spec
    private CommandSpec spec;

    Optional<String> name = Optional.empty();
    PocketType type;

    @Parameters(index = "0", completionCandidates = PocketType.PocketCandidates.class, description = "Type of pocket%n  Choices: ${COMPLETION-CANDIDATES}")
    void setPocketType(String value) {
        this.type = PocketType.fromParameter(value);
    }

    @ArgGroup(exclusive = false, heading = "%nPocket Attributes (required for custom pockets):%n")
    PocketAttributes attrs = new PocketAttributes();

    @Parameters(index = "1", description = "Name for your new pocket (max length = 50).", arity = "0..*")
    void setDescription(List<String> words) {
        name = Optional.of(String.join(" ", words));
    }

    @Override
    public Integer call() throws Exception {
        Term.debugf("Parameters: %s, %s, %s", type, name, attrs);

        Pocket pocket = createPocket();
        Term.outPrintf("%n✨ A new pocket named %s has been created with id '%s'.%n",
                pocket.name, pocket.id);

        if (Term.isVerbose()) {
            CommonIO.describe(pocket);
            CommonIO.listAllPockets();
        }

        return ExitCode.OK;
    }

    @Transactional
    Pocket createPocket() throws IOException {
        ParseResult pr = spec.commandLine().getParseResult();

        final Pocket pocket = type.createPocket(name);

        if (Term.canPrompt() && type == PocketType.Custom) {
            promptForAttributes(pocket, pr);
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
            pocket.magic = attrs.magic;
        }
        pocket.persist(); // <-- Save it!
        return pocket;
    }

    void promptForAttributes(Pocket pocket, ParseResult pr) throws IOException {
        String line = null;
        if (attrs.max_weight.isEmpty()) {
            line = Term.prompt("Enter the maximum weight of this pocket in pounds (e.g. 1, or 0.5): ");
            pocket.max_weight = Double.parseDouble(line);
        }
        if (attrs.max_volume.isEmpty()) {
            line = Term.prompt("Enter the maximum volume of this pocket in cubic feet (e.g. 2, or 0.75): ");
            pocket.max_volume = Double.parseDouble(line);
        }
        if (attrs.weight.isEmpty()) {
            line = Term.prompt("Weight of the pocket itself in pounds (e.g. 0.25, or 7): ");
            pocket.weight = Double.parseDouble(line);
        }
        if (!pr.hasMatchedOption("--magic")) {
            Term.outPrintln("Magic pockets always weigh the same, regardless of their contents.");
            line = Term.prompt("Is this a magic pocket (y/N)? ");
            pocket.magic = CommonIO.yesOrTrue(line, false);
        }
    }
}
