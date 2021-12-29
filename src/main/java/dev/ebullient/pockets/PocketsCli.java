package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@QuarkusMain
@Command(name = "pockets", header = "What have you got in your pockets?", subcommands = {
        PocketsCreate.class, PocketsEdit.class, PocketsOpen.class, PocketsDelete.class,
        PocketsList.class,
        PocketsItemAdd.class, PocketsItemUpdate.class, PocketsItemRemove.class
}, scope = ScopeType.INHERIT, mixinStandardHelpOptions = true, sortOptions = false, showDefaultValues = true, headerHeading = "%n", synopsisHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n", commandListHeading = "%nCommands:%n")
public class PocketsCli implements Callable<Integer>, QuarkusApplication {
    @Inject
    IFactory factory;

    @Spec
    private CommandSpec spec;

    @Option(names = { "--debug" }, description = "Enable debug output", scope = ScopeType.INHERIT)
    void setDebug(boolean debug) {
        Log.setDebug(debug);
    }

    @Option(names = { "--verbose" }, description = "Enable verbose output", scope = ScopeType.INHERIT)
    void setVerbose(boolean verbose) {
        Log.setVerbose(verbose);
    }

    @Option(names = { "--quiet" }, description = "Force quiet output", scope = ScopeType.INHERIT)
    void setQuiet(boolean quiet) {
        Log.setQuiet(quiet);
    }

    @Override
    public Integer call() {
        Log.showUsage(spec);
        return ExitCode.OK;
    }

    private int executionStrategy(ParseResult parseResult) {
        // Initialize log streams (after parameters have been read), carry on with the rest of the show
        Log.prepareStreams(parseResult.commandSpec());
        return new CommandLine.RunLast().execute(parseResult);
    }

    @Override
    @ActivateRequestContext
    public int run(String... args) {
        return new CommandLine(this, factory)
                .setCaseInsensitiveEnumValuesAllowed(true)
                .setExecutionStrategy(this::executionStrategy)
                .execute(args);
    }
}
