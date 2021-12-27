package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine;
import picocli.CommandLine.Command;
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
        Log.debugf("Parameters: %s", nameOrId);

        if (nameOrId.isEmpty()) {
            listAllPockets();
        } else {
            Optional<Long> id = Input.getId(nameOrId.get());
            Pocket pocket = id.isPresent()
                    ? selectPocketById(id.get())
                    : selectPocketByName(nameOrId.get());

            if (pocket == null) {
                listAllPockets();
            } else {
                listPocketContents(pocket);
            }
        }
        return CommandLine.ExitCode.OK;
    }

    public static Pocket selectPocketById(Long id) {
        Pocket pocket = Pocket.findById(id);
        if (pocket == null) {
            Log.outPrintf("%n[%s] doesn't match any of your pockets.%n", id);
        }
        return pocket;
    }

    public static Pocket selectPocketByName(String id) {
        List<Pocket> pockets = Pocket.findByName(id);
        if (pockets.size() > 1) {
            Log.outPrintf("%nSeveral pockets match '%s', which did you mean?%n%n", id);
            Log.outPrintln("@|faint [ ID ]    Name |@");
            Log.outPrintln("@|faint ------+--+-----------------------------------|@");
            pockets.forEach(p -> Log.outPrintf("[%4d] %2s %s\n", p.id, p.type.icon(), p.name));
            Log.outPrintln("");
        } else if (pockets.size() == 1) {
            return pockets.iterator().next();
        } else {
            Log.outPrintf("%n'%s' doesn't match any of your pockets.%n", id);
        }
        return null;
    }

    public static void listAllPockets() {
        List<Pocket> allPockets = Pocket.listAll();
        Log.outPrintln("\n 🛍  Your pockets:\n");
        Log.outPrintln("@|faint [ ID ]    Name |@");
        Log.outPrintln("@|faint ------+--+-----------------------------------|@");
        allPockets.forEach(p -> Log.outPrintf("[%4d] %2s %s\n", p.id, p.type.icon(), p.name));
        Log.outPrintln("");
    }

    public static void listPocketContents(Pocket pocket) {
        if (pocket.items.isEmpty()) {
            Log.outPrintf("%n%2s %s [%d] is empty.%n%n", pocket.type.icon(), pocket.name, pocket.id);
        } else {
            Log.outPrintf("%n%2s %s [%d] contains:%n%n", pocket.type.icon(), pocket.name, pocket.id);
            Log.outPrintln("@|faint [ ID ] ( Q )  Description |@");
            Log.outPrintln("@|faint ------+-----+------------------------------------|@");
            pocket.items.forEach(i -> Log.outPrintf("[%4d] @|faint (%3d)|@  %s\n", i.id, i.quantity, i.description));
            Log.outPrintln("");
        }
    }
}
