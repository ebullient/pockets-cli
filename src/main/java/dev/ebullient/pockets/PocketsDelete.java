package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "d", aliases = { "delete" }, header = "Delete a pocket (and all contained items and history)")
public class PocketsDelete implements Callable<Integer> {
    String nameOrId;

    @Option(names = { "-f", "--force" }, description = "Delete the specified pocket without confirmation", required = false)
    boolean force = false;

    @Parameters(index = "0", description = "Name or id of pocket to delete.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Optional<Long> id = CommonIO.toLong(nameOrId);
        Term.debugf("Parameters: %s, %s", nameOrId, id);

        Pocket pocket = id.isPresent()
                ? CommonIO.selectPocketById(id.get())
                : CommonIO.selectPocketByName(nameOrId);

        Term.debugf("Pocket to delete: %s", pocket);

        if (pocket == null) {
            return ExitCode.USAGE;
        } else {
            boolean deleteIt = force;
            if (Term.isVerbose()) {
                Term.outPrintf("%n%-2s %s [%d]:%n", pocket.type.icon(), pocket.name, pocket.id);
                CommonIO.describe(pocket);
                if (pocket.items == null || pocket.items.isEmpty() /* && pockets.pockets.isEmpty() */ ) {
                    Term.outPrintf("%n%s is empty.%n", pocket.name);
                } else {
                    Term.outPrintf("%n%s contains %s.%n", pocket.name,
                            CommonIO.howMany(pocket.items.size(), " pocket item", " pocket items"));
                }
            }

            if (!force && Term.canPrompt()) {
                String line = Term.prompt("Are you sure you want to delete this pocket (y|N)? ");
                deleteIt = CommonIO.yesOrTrue(line, false);
            }

            if (deleteIt) {
                pocket.delete();
                Term.outPrintf("%n✅ %s [%d] has been deleted.%n", pocket.name, pocket.id);
                if (Term.isVerbose()) {
                    CommonIO.dumpStatistics();
                }
            }
        }

        return ExitCode.OK;
    }
}
