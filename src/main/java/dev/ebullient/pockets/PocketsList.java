package dev.ebullient.pockets;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "list", mixinStandardHelpOptions = true, requiredOptionMarker = '*', showDefaultValues = true, header = "What do we have in our pockets?")
public class PocketsList implements Callable<Integer> {

    @Spec
    private CommandSpec spec;

    @Parameters(index = "0", description = "Id of Pocket to inspect.", arity = "0..1")
    Optional<Long> id;

    @Override
    public Integer call() throws Exception {
        Log.debugf("Parameters: %s", id);
        Help help = spec.commandLine().getHelp();

        Log.outPrintln("\n 🛍  Pockets:\n");
        if (id.isEmpty()) {
            List<Pocket> allPockets = Pocket.listAll();

            Map<String, String> map = new LinkedHashMap<>();
            map.put("[ID]", "NAME");

            map.putAll(allPockets.stream().collect(Collectors.toMap(
                    k -> Long.toString(k.id),
                    v -> String.format("%2s %s", v.type.icon(), v.name))));

            TextTable textTable = help.createTextTable(map);

            Log.outPrintln(textTable.toString());
        } else {
            Pocket pocket = Pocket.findById(id.get());

            if (pocket == null) {
                Log.outPrintln(id + " doesn't match any of your pockets\n");
            } else {
                Map<String, String> map = new LinkedHashMap<>();
                map.put("[ID]", "@|faint (Q)|@ DESCRIPTION");

                map.putAll(pocket.items.stream().collect(Collectors.toMap(
                        k -> Long.toString(k.id),
                        v -> String.format("@|faint (%d)|@ %s", v.quantity, v.description))));

                TextTable textTable = help.createTextTable(map);

                Log.outPrintln(String.format("[%s] %2s %s contains:\n", pocket.id, pocket.type.icon(), pocket.name));
                Log.outPrintln(textTable.toString());
            }
        }
        return CommandLine.ExitCode.OK;
    }
}
