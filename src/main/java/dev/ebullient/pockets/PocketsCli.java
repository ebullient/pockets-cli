package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "pockets", mixinStandardHelpOptions = true)
public class PocketsCli implements Callable<Integer> {

    @Parameters(paramLabel = "<name>", defaultValue = "picocli",
        description = "Your name.")
    String name;

    @Override
    public Integer call() {
        System.out.printf("Hello %s, go go commando!\n", name);
        return CommandLine.ExitCode.OK; // <--- Return USAGE, test will fail
    }
}
