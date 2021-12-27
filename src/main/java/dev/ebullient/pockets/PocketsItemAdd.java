package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
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

    static class ItemAttributes {
        @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add", defaultValue = "1", required = false)
        int quantity = 1;

        @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds", required = false)
        Optional<Double> weight = Optional.empty();

        @Option(names = { "-v",
                "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp)", required = false)
        Optional<String> gpValue = Optional.empty();
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Log.debugf("Parameters: %s, %s", pocketId, description);

        Pocket pocket = Pocket.findById(pocketId); // this is a full query.. maybe someday just ref
        if (pocket == null) {
            Log.outPrintf("Id %s doesn't match any of your pockets.%n", pocketId);
            PocketsList.listAllPockets();
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

            PocketsList.listPocketContents(pocket);
        }

        return CommandLine.ExitCode.OK;
    }
}
