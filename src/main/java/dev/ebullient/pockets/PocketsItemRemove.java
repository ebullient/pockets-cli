package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "r", aliases = { "remove" }, header = "Remove an item from a pocket")
public class PocketsItemRemove implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    @Option(names = { "-f",
            "--force" }, description = "Delete the specified pocket without confirmation", required = false)
    boolean force = false;

    String nameOrId;

    @Parameters(index = "1", description = "Name or id of item to delete from pocket.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Optional<Long> id = CommonIO.toLong(nameOrId, false);
        Term.debugf("Parameters: %s, %s -> %s", pocketId, nameOrId, id);

        Pocket pocket = CommonIO.selectPocketById(pocketId);
        if (pocket == null) {
            return ExitCode.USAGE;
        }

        PocketItem item = id.isPresent()
                ? CommonIO.selectPocketItemById(pocket, id.get())
                : CommonIO.selectPocketItemByName(pocket, nameOrId);

        Term.debugf("Pocket item to remove: %s", item);

        if (item == null) {
            return ExitCode.USAGE;
        }

        boolean deleteIt = force;
        if (Term.isVerbose()) {
            Term.outPrintf("%n%s [%d] contains @|faint (%d)|@ %s [%d]%n",
                    pocket.name, pocket.id, item.quantity, item.name, item.id);
        }

        if (!force && Term.canPrompt()) {
            String line = Term.prompt("Do you want to remove this item from this pocket? (y|N)? ");
            deleteIt = CommonIO.yesOrTrue(line, false);
        }

        if (deleteIt) {
            item.removeFromPocket(pocket);
            item.delete();

            if (item.quantity <= 1) {
                Term.outPrintf("%n✅ %s [%d] has been removed from %s [%d].%n", item.name, item.id, pocket.name, pocket.id);
            } else {
                Term.outPrintf("%n✅ @|faint (%d)|@ %s [%d] have been removed from %s [%d].%n", item.quantity, item.name,
                        item.id, pocket.name, pocket.id);
            }

            if (Term.isVerbose()) {
                CommonIO.listPocketContents(pocket);
            }
        } else if (!force) {
            Term.outPrintf("%n🔶 %s [%d] was not removed (requires confirmation or use --force).%n", item.name, item.id);
            return ExitCode.USAGE;
        }

        return ExitCode.OK;
    }

}
