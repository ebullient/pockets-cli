package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "d", aliases = { "delete" }, header = "Delete a pocket")
public class PocketDelete extends BaseCommand {

    String nameOrId;

    @Parameters(index = "0", description = "Name or id of pocket to delete.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {

        return ExitCode.OK;
    }
}
