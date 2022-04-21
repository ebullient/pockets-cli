package dev.ebullient.pockets;

import java.util.List;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "e", aliases = { "edit" }, header = "Edit a pocket.")
public class PocketEdit extends BaseCommand {

    @ArgGroup(exclusive = false, heading = "%nPocket Attributes:%n")
    PocketAttributes attrs = new PocketAttributes();

    String nameOrId;

    @Parameters(index = "0", description = "Name or id of pocket to edit.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    @Transactional
    public Integer call() throws Exception {
        Pocket pocket = selectPocketByNameOrId(nameOrId);
        if (pocket == null) {
            return PocketTui.NOT_FOUND;
        }
        tui.debugf("Pocket to edit: %s", pocket);

        Previous previous = new Previous(pocket);
        attrs.applyWithExisting(pocket, tui);

        if (previous.isUnchanged(pocket)) {
            tui.warnf("%s [%d] was not updated (no changes).%n", pocket.name, pocket.id);
            return ExitCode.OK;
        }

        if (tui.interactive()) {
            tui.outPrintln(tui.format().describe(pocket));
            if (!tui.reader().confirm()) {
                tui.warnf("%s [%d] was not updated.%n", pocket.name, pocket.id);
                return ExitCode.USAGE;
            }
        }

        pocket.persistAndFlush();
        tui.donef("%s [%d] has been updated.%n", pocket.name, pocket.id);
        tui.verbose(tui.format().describe(pocket));
        return ExitCode.OK;
    }

    class Previous {
        String name;
        double max_volume; // in cubic ft
        double max_weight; // in lbs
        double weight; // weight of the pocket itself
        boolean magic; // magic pockets always weigh the same

        Previous(Pocket pocket) {
            this.name = pocket.name;
            this.max_volume = pocket.max_volume;
            this.max_weight = pocket.max_weight;
            this.weight = pocket.weight;
            this.magic = pocket.extradimensional;
        }

        public boolean isUnchanged(Pocket pocket) {
            return this.name.equals(pocket.name)
                    && this.max_volume == pocket.max_volume
                    && this.max_weight == pocket.max_weight
                    && this.weight == pocket.weight
                    && this.magic == pocket.extradimensional;
        }
    }
}
