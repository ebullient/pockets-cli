package dev.ebullient.pockets;

import java.util.List;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.CommonIO.ItemAttributes;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "a", aliases = {
        "add" }, header = "Add an item to a pocket", description = Constants.ADD_DESCRIPTION, footer = {
                Constants.LIST_DESCRIPTION })
public class PocketsItemAdd implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    ItemAttributes attrs = new ItemAttributes();

    String name;

    @Parameters(index = "1", description = "Name (or description) of item to be added", arity = "1..*")
    void setName(List<String> words) {
        name = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Term.debugf("Parameters: %s, %s", pocketId, name);

        Pocket pocket = CommonIO.selectPocketById(pocketId);
        if (pocket == null) {
            return ExitCode.USAGE;
        }

        PocketItem item = new PocketItem();
        item.name = name;
        item.quantity = attrs.quantity;
        item.weight = attrs.weight.orElse(null);

        item.value = attrs.value.isPresent()
                ? CommonIO.gpValue(attrs.value.get(), true).orElse(null)
                : null;

        item.addToPocket(pocket);
        item.persistAndFlush();

        Term.outPrintf("%n✨ @|faint (%d)|@ %s [%s] added to %s [%s]%n",
                item.quantity, item.name, item.id, pocket.name, pocket.id);

        if (Term.isVerbose()) {
            CommonIO.listPocketContents(pocket);
        }
        return ExitCode.OK;
    }
}
