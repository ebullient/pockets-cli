package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketsFormat.*;

import picocli.CommandLine.Command;

@Command(name = "tx", aliases = { "move", "trade" },
        header = MOVE + NBSP + "Move or trade items.")
public class MoveInventory extends BaseCommand {

    @Override
    public Integer delegateCall() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateCall'");
    }

}
