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
        Term.setDebug(debug);
    }

    @Option(names = { "--verbose" }, description = "Enable verbose output", scope = ScopeType.INHERIT)
    void setVerbose(boolean verbose) {
        Term.setVerbose(verbose);
    }

    @Option(names = { "--quiet" }, description = "Force quiet output", scope = ScopeType.INHERIT)
    void setQuiet(boolean quiet) {
        Term.setQuiet(quiet);
    }

    @Override
    public Integer call() {
        Term.showUsage(spec);
        return ExitCode.OK;
    }

    private int executionStrategy(ParseResult parseResult) {
        // Initialize log streams (after parameters have been read), carry on with the rest of the show
        Term.prepareStreams(parseResult.commandSpec());
        int result = new CommandLine.RunLast().execute(parseResult);
        Term.close();
        return result;
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
