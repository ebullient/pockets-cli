package dev.ebullient.pockets;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;

@Command(name = "e", aliases = { "edit" }, header = "Edit a pocket")
public class PocketEdit extends BaseCommand {

    @ArgGroup(exclusive = false, heading = "%nPocket Attributes:%n")
    PocketAttributes attrs = new PocketAttributes();

    String nameOrId;

    @Parameters(index = "0", description = "Name or id of pocket to edit.", arity = "1..*")
    void setNameOrId(List<String> words) {
        nameOrId = String.join(" ", words);
    }

    @Override
    public Integer call() throws Exception {

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
