package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import javax.transaction.Transactional;

import picocli.CommandLine.Command;

@Command(name = "r", aliases = { "remove" }, header = "Remove an item from a pocket", description = Constants.ADD_DESCRIPTION)
public class PocketsItemRemove implements Callable<Integer> {

    @Override
    @Transactional
    public Integer call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
