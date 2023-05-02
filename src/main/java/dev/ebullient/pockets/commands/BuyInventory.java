package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketsFormat.LOOT;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import picocli.CommandLine.Command;

@Command(name = "buy", aliases = { "buy" },
        header = LOOT + NBSP + "Exchange currency for pockets and/or items.")
public class BuyInventory extends BaseCommand {

    @Override
    public Integer delegateCall() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateCall'");
    }

}
