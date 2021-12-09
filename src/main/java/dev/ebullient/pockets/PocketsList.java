package dev.ebullient.pockets;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.CommandSpec;

@Command(name = "list",
    mixinStandardHelpOptions = true, requiredOptionMarker = '*', showDefaultValues = true,
    header = "What do we have in our pockets?")
public class PocketsList implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "Id of Pocket to inspect.", arity = "0..1")
    Optional<Long> id;

    @Override
    public Integer call() throws Exception {
        Log.debugf("Parameters: %s", id);
        Help help = spec.commandLine().getHelp();

        if ( id.isEmpty() ) {
            List<Pocket> allPockets = Pocket.listAll();
            Log.debug(allPockets.toString());
            TextTable textTable = help.createTextTable(
                    allPockets.stream().collect(Collectors.toMap(k -> k.id, v -> v.name)));
            Log.outPrintln("\nPockets:\n");
            Log.outPrintln(textTable.toString());
        } else  {
            Pocket pocket = Pocket.findById(id.get());
            if ( pocket == null ) {
                Log.outPrintln(id  + " doesn't match any of your pockets");
            } else {
                Log.outPrintln(String.format("%s (%s) contains:", pocket.name, pocket.id));
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
