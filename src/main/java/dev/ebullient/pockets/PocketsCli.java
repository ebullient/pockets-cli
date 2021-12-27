package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import javax.enterprise.context.control.ActivateRequestContext;
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
@Command(name = "pockets", subcommands = {
        PocketsCreate.class, PocketsEdit.class, PocketsOpen.class, PocketsDelete.class,
        PocketsList.class,
        PocketsItemAdd.class, PocketsItemUpdate.class, PocketsItemRemove.class
}, scope = ScopeType.INHERIT, mixinStandardHelpOptions = true, sortOptions = false, showDefaultValues = true, headerHeading = "%n", synopsisHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n", commandListHeading = "%nCommands:%n")
public class PocketsCli implements Callable<Integer>, QuarkusApplication {

    @Inject
    IFactory factory;

    @Option(names = { "--debug" }, description = "verbose/debug output", scope = ScopeType.INHERIT)
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
    @ActivateRequestContext
    public int run(String... args) throws Exception {
        int result = new CommandLine(this, factory)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionStrategy(this::executionStrategy)
                .execute(args);
        return result;
    }
}
