package dev.ebullient.pockets;

import java.util.List;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "a", aliases = { "add" }, header = "Add an item to a pocket")
public class ItemAdd extends BaseCommand {

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    ItemAttributes attrs = new ItemAttributes();

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    String name;

    @Parameters(index = "1", description = "Name (or description) of item to be added", arity = "1..*")
    void setName(List<String> words) {
        name = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Pocket pocket = selectPocketById(pocketId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }

        Item item = new Item();
        item.name = name;
        item.quantity = attrs.quantity.orElse(1);
        item.weight = attrs.weight.orElse(null);
        item.tradable = attrs.tradable.orElse(true);

        item.gpValue = attrs.value.isPresent()
                ? Mapper.Currency.gpValue(attrs.value.get(), tui).orElse(null)
                : null;

        item.addToPocket(pocket);
        item.persistAndFlush();

        tui.donef("@|faint (%d)|@ %s [%s] added to %s [%s]%n",
                item.quantity, item.name, item.id, pocket.name, pocket.id);

        tui.verbose(tui.format().describe(pocket));
        return ExitCode.OK;
    }
}
