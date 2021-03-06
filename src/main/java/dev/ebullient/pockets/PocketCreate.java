package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.index.PocketReference;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "c", aliases = { "create" }, header = "Create a new pocket.")
public class PocketCreate extends BaseCommand {

    @Inject
    Index index;

    @ArgGroup(exclusive = false, heading = "%nPocket Attributes (required for custom pockets):%n")
    PocketAttributes attrs = new PocketAttributes();

    @Parameters(index = "0", description = "Specify the type of pocket 'Pocket Type' or 'pocket-type' (see --types). Use quotes if there are spaces.")
    String pocketRef;

    @Option(names = { "--types" }, description = "List pocket types and exit.", help = true)
    boolean showTypes;

    Optional<String> name = Optional.empty();

    @Parameters(index = "1", description = "Name for your new pocket (max length = 50).", arity = "0..*")
    void setName(List<String> words) {
        name = Optional.of(String.join(" ", words));
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        if (showTypes) {
            index.listPocketTypes();
            return ExitCode.OK;
        }
        if (tui.interactive() && pocketRef == null) {
            index.listPocketTypes();
            pocketRef = tui.reader().prompt("Specify pocket type (leave blank for custom)");
        }
        if (tui.interactive() && name == null) {
            name = Optional.of(tui.reader().prompt("Specify a name for this pocket (leave blank to use the Pocket's name)"));
        }

        final PocketReference pRef = index.getPocketReference(pocketRef);
        final Pocket pocket = pRef.createPocket(name);
        attrs.applyWithDefaults(pocket, pRef, tui);

        pocket.persist(); // <-- Save it!

        tui.createf("A new pocket named %s has been created with id '%s'.%n",
                pocket.name, pocket.id);

        if (tui.isVerbose()) {
            tui.outPrintln(tui.format().describe(pocket));
            listAllPockets();
        }
        return ExitCode.OK;
    }
}
