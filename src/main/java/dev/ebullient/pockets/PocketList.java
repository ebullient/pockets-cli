package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "l", aliases = { "list" }, header = "What do we have in our pockets?")
public class PocketList extends BaseCommand {

    Optional<String> nameOrId = Optional.empty();

    @Parameters(index = "0", description = "Name or ID of pocket to inspect.", arity = "0..*")
    void setNameOrId(List<String> words) {
        nameOrId = Optional.of(String.join(" ", words));
    }

    @Override
    public Integer call() throws Exception {

        if (nameOrId.isEmpty()) {
            listAllPockets();
        } else {
            Pocket pocket = selectPocketByNameOrId(nameOrId.get());
            if (pocket == null) {
                return PocketTui.NOT_FOUND;
            } else {
                tui.outPrintln(tui.format().describe(pocket));
            }
        }
        return ExitCode.OK;
    }
}
