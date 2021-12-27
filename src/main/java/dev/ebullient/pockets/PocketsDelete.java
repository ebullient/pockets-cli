package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine;
import picocli.CommandLine.Command;
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
        Optional<Long> id = Input.getId(nameOrId);
        Log.debugf("Parameters: %s, %s", nameOrId, id);

        Pocket pocket = id.isPresent()
                ? PocketsList.selectPocketById(id.get())
                : PocketsList.selectPocketByName(nameOrId);

        Log.debugf("Pocket to delete: %s", pocket);

        if (pocket == null) {
            Log.outPrintf("%s doesn't match any of your pockets.%n", nameOrId);
            PocketsList.listAllPockets();
            return CommandLine.ExitCode.USAGE;
        } else {
            boolean deleteIt = force;
            if (!force) {
                Log.outPrintf("%n%2s %s [%d]:%n", pocket.type.icon(), pocket.name, pocket.id);
                pocket.describe();

                LineReader reader = LineReaderBuilder.builder().build();
                String line = reader.readLine("Are you sure you want to delete this pocket (y|N)? ");
                deleteIt = Input.yesOrTrue(line, false);
            }

            if (deleteIt) {
                pocket.delete();
                Log.outPrintf("%n✅ The pocket named %s has been deleted.%n", pocket.name);
            }
        }

        return CommandLine.ExitCode.OK;
    }
}
