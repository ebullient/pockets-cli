package dev.ebullient.pockets;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@TopCommand
@Command(name = "greeting", mixinStandardHelpOptions = true)
public class PocketsCli implements Runnable {

    @Parameters(paramLabel = "<name>", defaultValue = "picocli",
        description = "Your name.")
    String name;

    @Override
    public void run() {
        System.out.printf("Hello %s, go go commando!\n", name);
    }

}
