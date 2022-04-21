package dev.ebullient.pockets;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "a", aliases = { "add" }, header = "Add an item to a pocket.")
public class ItemAdd extends BaseCommand {

    @Inject
    Index index;

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    ItemAttributes attrs = new ItemAttributes();

    @Option(names = { "--types" }, description = "List item types and exit.", help = true)
    boolean showTypes;

    @Parameters(index = "0", description = "Id of the target Pocket.")
    Long pocketId;

    String name;

    @Parameters(index = "1", description = "Name (or description) of item to be added.", arity = "1..*")
    void setName(List<String> words) {
        name = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        if (showTypes) {
            index.listPocketTypes();
            return ExitCode.OK;
        }

        Pocket pocket = selectPocketById(pocketId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }

        Item item = new Item();
        item.name = name;
        attrs.applyWithDefaults(item, null, tui);

        if (tui.interactive()) {
            tui.outPrintf("%s [%d] will be added with the following attributes:%n", item.name, item.id);
            tui.outPrintln(tui.format().describe(item));
            if (!tui.reader().confirm("Do you want to add this item to this pocket")) {
                tui.warnf("%s was not added to %s [%d].%n", item.name, pocket.name, pocket.id);
                tui.verbose(tui.format().describe(pocket));
                return ExitCode.USAGE;
            }
        }

        item.addToPocket(pocket);
        item.persistAndFlush();

        tui.donef("@|faint (%d)|@ %s [%d] added to %s [%d]%n",
                item.quantity, item.name, item.id, pocket.name, pocket.id);

        tui.verbose(tui.format().describe(pocket));
        return ExitCode.OK;
    }
}
