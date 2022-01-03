package dev.ebullient.pockets;

import java.util.List;
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
import picocli.CommandLine.Spec;

@Command(name = "a", aliases = {
        "add" }, header = "Add an item to a pocket", description = Constants.ADD_DESCRIPTION, footer = {
                Constants.LIST_DESCRIPTION })
public class PocketItemAdd implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Inject
    CommonIO io;

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    @Option(names = { "-f", "--force" }, description = "Add item without confirmation or prompting", required = false)
    boolean force = false;

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    PocketItemAttributes attrs = new PocketItemAttributes();

    String name;

    @Parameters(index = "1", description = "Name (or description) of item to be added", arity = "1..*")
    void setName(List<String> words) {
        name = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Term.debugf("Parameters: %s, %s", pocketId, name);

        Pocket pocket = io.selectPocketById(pocketId);
        if (pocket == null) {
            return ExitCode.USAGE;
        }

        PocketItem item = new PocketItem();
        item.name = name;
        item.quantity = attrs.quantity;
        item.weight = attrs.weight.orElse(null);

        item.gpValue = attrs.value.isPresent()
                ? CommonIO.gpValue(attrs.value.get(), true).orElse(null)
                : null;

        boolean addIt = force;
        int result = ExitCode.OK;

        if (!force && Term.canPrompt()) {
            String line = Term.prompt("Do you want to add this item to this pocket? (y|N)? ");
            addIt = CommonIO.yesOrTrue(line, false);
        }
        if (addIt) {
            item.addToPocket(pocket);
            item.persistAndFlush();
            io.checkFieldWidths(item);

            Term.outPrintf("%n✨ @|faint (%d)|@ %s [%s] added to %s [%s]%n",
                    item.quantity, item.name, item.id, pocket.name, pocket.id);
        } else if (!force) {
            Term.outPrintf("%n🔶 %s was not added (requires confirmation or use --force).%n", item.name);
            result = ExitCode.USAGE;
        }

        if (Term.isVerbose()) {
            io.listPocketContents(pocket);
        }
        return result;
    }
}
