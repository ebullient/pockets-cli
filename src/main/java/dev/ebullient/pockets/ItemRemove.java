package dev.ebullient.pockets;

import java.util.List;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "r", aliases = { "remove" }, header = "Remove an item from a pocket.")
public class ItemRemove extends BaseCommand {

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    String nameOrId;

    @Parameters(index = "1", description = "Name or id of item to remove from the pocket.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Pocket pocket = selectPocketById(pocketId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }

        Item item = selectItemByNameOrId(pocket, nameOrId);
        if (item == null) {
            return PocketTui.NOT_FOUND;
        }

        if ( tui.interactive() ) {
            tui.outPrintf("%n%s [%d] contains @|faint (%d)|@ %s [%d]%n",
                pocket.name, pocket.id, item.quantity, item.name, item.id);
            if (!tui.reader().confirm("Do you want to remove this item from this pocket")) {
                tui.warnf("%s [%d] was not removed from %s [%d].%n",
                    item.name, item.id, pocket.name, pocket.id);
                tui.verbose(tui.format().describe(pocket));
                return ExitCode.USAGE;
            }
        }

        item.removeFromPocket(pocket);
        item.delete();

        if (item.quantity <= 1) {
            tui.donef("%s [%d] has been removed from %s [%d].%n", item.name, item.id, pocket.name, pocket.id);
        } else {
            tui.donef("@|faint (%d)|@ %s [%d] have been removed from %s [%d].%n", item.quantity, item.name,
                item.id, pocket.name, pocket.id);
        }

        tui.verbose(tui.format().describe(pocket));
        return ExitCode.OK;
    }
}
