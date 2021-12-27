package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "u", aliases = { "update" }, header = "Update an item in a pocket", description = Constants.ADD_DESCRIPTION)
public class PocketsItemUpdate implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
