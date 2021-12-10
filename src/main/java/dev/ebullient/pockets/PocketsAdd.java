package dev.ebullient.pockets;

import java.util.List;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "add", mixinStandardHelpOptions = true, requiredOptionMarker = '*', showDefaultValues = true, header = "Add an item to a pocket")
public class PocketsAdd implements Callable<Integer> {

    String description;

    @Parameters(index = "0", description = "Id of Pocket to add item to.")
    Long id;

    @Parameters(index = "1", description = "Description of item to be added", arity = "1..*")
    void setDescription(List<String> words) {
        description = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Log.debugf("Parameters: %s, %s", id, description);

        Pocket pocket = Pocket.findById(id);
        if (pocket == null) {
            Log.outPrintln(id + " doesn't match any of your pockets.");
            // TODO: Helpfully list our pockets!
        } else {
            PocketItem item = new PocketItem();
            item.description = description;
            item.quantity = 1;
            item.weight = 0;
            item.value = 0;
            // TODO: Options for setting quantity, weight, value

            pocket.addPocketItem(item);
            pocket.persist();
        }

        return CommandLine.ExitCode.OK;
    }
}
