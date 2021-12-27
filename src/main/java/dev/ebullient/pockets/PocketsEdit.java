package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "e", aliases = { "edit" }, header = "Edit the attributes of a pocket")
public class PocketsEdit implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
