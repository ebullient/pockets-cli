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
    String description;

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    ItemAttributes attrs = new ItemAttributes();

    @Parameters(index = "1", description = "Description of item to be added", arity = "1..*")
    void setDescription(List<String> words) {
        description = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Log.debugf("Parameters: %s, %s", pocketId, description);

        Pocket pocket = Pocket.findById(pocketId); // this is a full query.. maybe someday just ref
        if (pocket == null) {
            Log.outPrintf("Id %s doesn't match any of your pockets.%n", pocketId);
            CommonIO.listAllPockets();
        } else {
            PocketItem item = new PocketItem();
            item.description = description;
            item.quantity = attrs.quantity;
            item.weight = attrs.weight.orElse(null);
            item.value = attrs.gpValue.map(v -> Coinage.gpValue(spec.commandLine(), v)).orElse(null);

            item.addToPocket(pocket);
            item.persistAndFlush();

            Log.outPrintf("%n@|faint (%d)|@ %s [%s] added to %s [%s]%n",
                    item.quantity, item.description, item.id, pocket.name, pocket.id);

            CommonIO.listPocketContents(pocket);
        }

        return ExitCode.OK;
    }
}
