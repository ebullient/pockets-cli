package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "u", aliases = { "update" }, header = "Update an item in a pocket")
public class ItemUpdate extends BaseCommand {

    @ArgGroup(exclusive = false, heading = "%nItem attributes:%n")
    ItemAttributes attrs = new ItemAttributes();

    @Parameters(index = "0", description = "Id of the target Pocket")
    Long pocketId;

    String nameOrId;

    @Parameters(index = "1", description = "Name or id of item to delete from pocket.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {

        return ExitCode.OK;
    }

    class Previous {
        public String name;
        public int quantity;
        public Double weight; // weight in lbs
        public Double value; // value in gp

        Previous(Item item) {
            this.quantity = item.quantity;
            this.name = item.name;
            this.value = item.gpValue;
            this.weight = item.weight;
        }

        public boolean isUnchanged(Item item) {
            return this.name.equals(item.name)
                    && this.quantity == item.quantity
                    && this.value == item.gpValue
                    && this.weight == item.weight;
        }
    }
}
