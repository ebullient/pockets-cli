package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketsFormat.BYE;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import picocli.CommandLine.Command;

@Command(name = "rm", aliases = { "consume", "remove" },
        header = BYE + NBSP + "Remove pockets, items, or currency.",
        description = "%nConsume a potion, give a gift, or explode a bag of holding.")
public class RemoveInventory extends BaseCommand {

    @Override
    public Integer delegateCall() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delegateCall'");
    }
}
