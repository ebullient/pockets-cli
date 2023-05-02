package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketsFormat.LOOK;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import picocli.CommandLine.Command;

@Command(name = "ls", aliases = { "list" }, header = LOOK + NBSP + "Look at your stuff.")
public class ListInventory extends BaseCommand {

    @Override
    public Integer delegateCall() throws Exception {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateCall'");
    }
}
