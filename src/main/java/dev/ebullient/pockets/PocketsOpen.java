package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;

@Command(name = "o", aliases = { "open" }, header = "Open a pocket (interactive)", description = "%n" +
        "Open an interactive terminal session to work with a specific pocket")
public class PocketsOpen implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
