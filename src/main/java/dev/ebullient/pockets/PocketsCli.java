package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;

@TopCommand
@Command(name = "pockets", header = "What have you got in your pockets?",
    mixinStandardHelpOptions = true,
    scope = ScopeType.INHERIT)
public class PocketsCli implements Callable<Integer> {

    @Parameters(paramLabel = "<name>", defaultValue = "picocli",
        description = "Your name.")
    String name;

    @Override
    public Integer call() throws Exception {
        System.out.printf("Hello %s, go go commando!\n", name);
        return ExitCode.OK;
    }
}
