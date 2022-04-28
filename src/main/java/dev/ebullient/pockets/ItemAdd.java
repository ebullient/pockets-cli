package dev.ebullient.pockets;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Currency;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.index.ItemReference;
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

    @Parameters(index = "0", description = "Name or ID of the target Pocket. Use quotes if there are spaces.")
    String pocketId;

    @Option(names = { "--type" }, description = "Item reference type (see --types)")
    String type;

    String name;

    @Parameters(index = "1", description = "Type or description of item to be added.", arity = "1..*")
    void setName(List<String> words) {
        name = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        if (showTypes) {
            index.listItemTypes();
            return ExitCode.OK;
        }

        Pocket pocket = selectPocketByNameOrId(pocketId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }

        ItemReference iRef = null;
        if (type != null) {
            iRef = findItemReference(type);
        } else {
            iRef = findItemReference(name);
        }

        Item item = null;
        if (iRef == null) {
            item = new Item();
            item.name = name;
            attrs.applyWithDefaults(item, tui);
        } else {
            item = iRef.createItem(name);
            attrs.applyWithExisting(item, tui);
        }

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

        tui.donef("@|faint (%d)|@ %s [%d] %s%sadded to %s [%d]",
                item.quantity, item.name, item.id,
                item.weight == null ? "" : String.format("(%s lbs) ", item.weight),
                item.cpValue == null ? "" : String.format("(%s gp) ", item.cpValue * Currency.cp.gpEx),
                pocket.name, pocket.id);
        tui.verbose(tui.format().describe(pocket));
        return ExitCode.OK;
    }

    ItemReference findItemReference(String name) {
        if (tui.interactive() && name == null) {
            name = tui.reader().prompt("Specify a name for (or the type of) this item");
        }

        List<ItemReference> references = index.findItemReference(name);
        if (references.size() == 1) {
            return references.iterator().next();
        } else if (references.size() > 1) {
            tui.outPrintf("%nThe specified value [%s] matches more than one item:%n", name);
            tui.outPrintln(tui.format().table("item-type", "Item Type", references.stream()
                    .collect(Collectors.toMap(x -> x.idSlug, x -> x.name, (o1, o2) -> o1, TreeMap::new))));

            if (tui.interactive()) {
                return findItemReference(null);
            }
        }
        return null;
    }
}
