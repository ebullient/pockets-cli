package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

public class BaseCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Inject
    PocketTui tui;

    @Override
    public Integer call() throws Exception {
        tui.showUsage(spec);
        return ExitCode.OK;
    }

    public void listAllPockets() {
        List<Pocket> allPockets = Pocket.listAll();
        tui.outPrintln(tui.format().listPockets(allPockets));
    }

    public Pocket selectPocketByNameOrId(String nameOrId) {
        Optional<Long> id = Mapper.toLong(nameOrId);

        return id.isPresent()
            ? selectPocketById(id.get())
            : selectPocketByName(nameOrId);
    }

    public Pocket selectPocketById(Long pocketId) {
        Pocket pocket = Pocket.findById(pocketId);
        if (pocket == null) {
            tui.outPrintf("%nThe specified value [%s] doesn't match any of your pockets.%n", pocketId);
            listAllPockets();
            return null;
        }
        return pocket;
    }

    public Pocket selectPocketByName(String name) {
        List<Pocket> pockets = Pocket.findByName(name);

        if (pockets.size() == 1) {
            return pockets.iterator().next();
        }

        if (pockets.size() > 1) {
            tui.outPrintf("%nThe specified value [%s] matches more than one pocket.%n", name);
        } else  {
            tui.outPrintf("%n'%s' doesn't match any of your pockets.%n", name);
        }
        listAllPockets();
        return null;
    }
}
