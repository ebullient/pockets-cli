package dev.ebullient.pockets;

import java.io.File;
import java.util.concurrent.Callable;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.inject.Inject;

import dev.ebullient.pockets.reference.Convert5eTools;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@QuarkusMain
@Command(name = "pockets", header = "What have you got in your pockets?", subcommands = {
        PocketsCreate.class, PocketsEdit.class, PocketsOpen.class, PocketsDelete.class,
        PocketsList.class,
        PocketItemAdd.class, PocketItemUpdate.class, PocketItemRemove.class,
        Convert5eTools.class
}, scope = ScopeType.INHERIT, mixinStandardHelpOptions = true, sortOptions = false, showDefaultValues = true, headerHeading = "%n", synopsisHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n", commandListHeading = "%nCommands:%n")
public class PocketsCli implements Callable<Integer>, QuarkusApplication {
    @Inject
    IFactory factory;

    @Spec
    private CommandSpec spec;

    @Inject
    Config config;

    @Option(names = { "-d", "--debug" }, description = "Enable debug output", scope = ScopeType.INHERIT)
    void setDebug(boolean debug) {
        Term.setDebug(debug);
    }

    @Option(names = { "-b", "--brief" }, description = "Brief output", scope = ScopeType.INHERIT)
    void setBrief(boolean brief) {
        Term.setBrief(brief);
    }

    @Option(names = { "--config" }, description = "Config directory. Default is ~/.pockets", scope = ScopeType.INHERIT)
    void setConfig(File configDir) {
        if (configDir.exists() && configDir.isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + configDir.toString());
        }
        config.setConfigPath(configDir);
    }

    @Override
    public Integer call() {
        Term.showUsage(spec);
        return ExitCode.OK;
    }

    private int executionStrategy(ParseResult parseResult) {
        // Initialize log streams (after parameters have been read), carry on with the rest of the show
        Term.prepareStreams(parseResult.commandSpec());
        config.init();
        int result = new CommandLine.RunLast().execute(parseResult);
        config.close();
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
