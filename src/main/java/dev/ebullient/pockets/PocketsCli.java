package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import javax.enterprise.context.control.ActivateRequestContext;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.io.PocketTui;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@QuarkusMain
@Command(name = "pockets", header = "What have you got in your pockets?",
    subcommands = { PocketCreate.class, PocketList.class },
    mixinStandardHelpOptions = true,
    scope = ScopeType.INHERIT)
public class PocketsCli implements Callable<Integer>, QuarkusApplication {

    final PocketTui tui = new PocketTui();
    final Index index = new Index(tui);

    @Spec
    CommandSpec spec;

    @Inject
    IFactory factory;

    @Option(names = { "-d", "--debug" }, description = "Enable debug output", scope = ScopeType.INHERIT)
    boolean debug;

    @Option(names = { "-b", "--brief" }, description = "Brief output", scope = ScopeType.INHERIT)
    boolean brief;

    @Override
    public Integer call() throws Exception {
        // invocation of `pockets` command
        tui.showUsage(spec);
        return ExitCode.OK;
    }

    private int executionStrategy(ParseResult parseResult) {
        init(parseResult);
        int result = new CommandLine.RunLast().execute(parseResult);
        shutdown();
        return result;
    }

    private void init(ParseResult parseResult) {
        tui.init(spec, debug, !brief);
        index.init();
        tui.format().setIndex(index); // reference for formatting

        tui.debug("HERE WE ARE: INIT");
    }

    private void shutdown() {
        tui.debug("HERE WE ARE: SHUTDOWN");
    }

    @Override
    @ActivateRequestContext
    public int run(String... args) throws Exception {
        return new CommandLine(this, factory)
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setExecutionStrategy(this::executionStrategy)
            .execute(args);
    }

    @Produces
    PocketTui getTui() {
        return tui;
    }

    @Produces
    Index getIndex() {
        return index;
    }
}
