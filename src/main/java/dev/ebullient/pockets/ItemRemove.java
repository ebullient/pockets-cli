package dev.ebullient.pockets;

import java.util.List;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "r", aliases = { "remove" }, header = "Remove an item from a pocket")
public class ItemRemove extends BaseCommand {

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    String nameOrId;

    @Parameters(index = "1", description = "Name or id of item to delete from pocket.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {


        return ExitCode.OK;
    }
}
