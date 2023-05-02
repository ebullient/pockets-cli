package dev.ebullient.pockets;

import java.util.List;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import jakarta.transaction.Transactional;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "u", aliases = { "update" }, header = "Update items in a pocket.")
public class ItemUpdate extends BaseCommand {

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    ItemAttributes attrs = new ItemAttributes();

    @Parameters(index = "0", description = "Name or ID of the target Pocket. Use quotes if there are spaces.")
    String pocketId;

    String nameOrId;

    @Parameters(index = "1", description = "Name or id of item to delete from pocket.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Pocket pocket = selectPocketByNameOrId(pocketId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }
        Item item = selectItemByNameOrId(pocket, nameOrId);
        if (item == null) {
            return PocketTui.NOT_FOUND;
        }

        Previous previous = new Previous(item);
        attrs.applyWithExisting(item, tui);

        if (previous.isUnchanged(item)) {
            tui.warnf("%s [%d] was not updated (no changes).%n", item.name, item.id);
            return ExitCode.OK;
        }

        if (tui.interactive()) {
            tui.outPrintf("%s [%d] will be updated with the following attributes:%n", item.name, item.id);
            tui.outPrintln(tui.format().describe(item));
            if (!tui.reader().confirm()) {
                tui.warnf("%s [%d] was not updated.%n", item.name, item.id);
                tui.verbose(tui.format().describe(pocket));
                return ExitCode.USAGE;
            }
        }

        item.persistAndFlush();
        tui.donef("%s [%d] has been updated.%n", item.name, item.id);

        tui.verbose(tui.format().describe(pocket));
        return ExitCode.OK;
    }

    class Previous {
        public String name;
        public int quantity;
        public Double weight; // weight in lbs
        public Double value; // value in cp

        Previous(Item item) {
            this.quantity = item.quantity;
            this.name = item.name;
            this.value = item.cpValue;
            this.weight = item.weight;
        }

        public boolean isUnchanged(Item item) {
            return this.name.equals(item.name)
                    && this.quantity == item.quantity
                    && this.value == item.cpValue
                    && this.weight == item.weight;
        }
    }
}
