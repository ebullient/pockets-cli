package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;

@QuarkusMain
@Command(name = "pockets", mixinStandardHelpOptions = true, subcommands = {
    PocketsCreate.class
})
public class PocketsCli implements Callable<Integer>, QuarkusApplication {

    @Inject
    IFactory factory;

    @Option(names = { "--verbose", "-v" }, description = "verbose output", scope = ScopeType.INHERIT)
    void setVerbose(boolean verbose) {
        Log.setVerbose(verbose);
    }

    @Override
    public Integer call() {
        return CommandLine.ExitCode.OK;
    }

    private int executionStrategy(ParseResult parseResult) {
        // Initialize log streams, carry on with the rest of the show
        Log.prepareStreams(parseResult.commandSpec());
        return new CommandLine.RunLast().execute(parseResult);
    }

    @Override
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory)
            .setExecutionStrategy(this::executionStrategy)
            .execute(args);
    }
}
