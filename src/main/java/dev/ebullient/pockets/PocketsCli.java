package dev.ebullient.pockets;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import dev.ebullient.pockets.reference.Import;
import dev.ebullient.pockets.reference.Index;
import io.quarkus.runtime.LaunchMode;
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
        Import.class
}, scope = ScopeType.INHERIT, mixinStandardHelpOptions = true, sortOptions = false, showDefaultValues = true, headerHeading = "%n", synopsisHeading = "%n", parameterListHeading = "%nParameters:%n", optionListHeading = "%nOptions:%n", commandListHeading = "%nCommands:%n")
public class PocketsCli implements Callable<Integer>, QuarkusApplication {
    @Inject
    IFactory factory;

    @Spec
    private CommandSpec spec;

    File configDirectory;
    PocketsCache cache;
    Index index;

    public PocketsCli() {
        if (LaunchMode.current().isDevOrTest()) {
            configDirectory = Path.of(System.getProperty("user.dir"), "target/.pockets").toFile();
        } else {
            configDirectory = Path.of(System.getProperty("user.home"), ".pockets").toFile();
        }
        Term.debug("Default config directory: " + configDirectory);
    }

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
                    "Specified output path exists and is a file: " + configDir);
        }
        this.configDirectory = configDir;
    }

    @Override
    public Integer call() {
        Term.showUsage(spec);
        return ExitCode.OK;
    }

    private void init(ParseResult parseResult) {
        Term.prepareStreams(parseResult.commandSpec());
        configDirectory.mkdirs();
        cache = new PocketsCache.Builder()
                .setConfigDirectory(configDirectory)
                .build();

        index = new Index.Builder()
                .setConfigDirectory(configDirectory)
                .build();
    }

    private void close() {
        cache.persist();
        Term.close();
    }

    private int executionStrategy(ParseResult parseResult) {
        // Initialize log streams and caches after parameters have been read,
        // then carry on with the rest of the show
        init(parseResult);
        int result = new CommandLine.RunLast().execute(parseResult);
        close();
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

    @Produces
    PocketsCache getCache() {
        return cache;
    }

    @Produces
    Index getIndex() {
        return index;
    }
}
