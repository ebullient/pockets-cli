package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "l", aliases = { "list" }, header = "List all pockets, or the contents of one pocket", footer = {
        Constants.LIST_DESCRIPTION })
public class PocketsList implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    Optional<String> nameOrId = Optional.empty();

    @Parameters(index = "0", description = "Name or id of pocket to inspect.", arity = "0..*")
    void setNameOrId(List<String> words) {
        nameOrId = Optional.of(String.join(" ", words));
    }

    @Override
    public Integer call() throws Exception {
        LineReader reader = LineReaderBuilder.builder().build();
        Log.debugf("Parameters: %s", nameOrId);

        if (nameOrId.isEmpty()) {
            CommonIO.listAllPockets();
        } else {
            Optional<Long> id = CommonIO.getId(nameOrId.get());
            Pocket pocket = id.isPresent()
                    ? CommonIO.selectPocketById(id.get())
                    : CommonIO.selectPocketByName(nameOrId.get(), reader);

            if (pocket == null) {
                return ExitCode.USAGE;
            } else {
                CommonIO.listPocketContents(pocket);
            }
        }
        return ExitCode.OK;
    }
}
