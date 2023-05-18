package dev.ebullient.pockets;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.BOOM;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import java.io.PrintWriter;
import java.util.concurrent.Callable;

import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.apache.camel.quarkus.main.CamelMain;

import dev.ebullient.pockets.commands.AddItem;
import dev.ebullient.pockets.commands.BuyInventory;
import dev.ebullient.pockets.commands.CreateItem;
import dev.ebullient.pockets.commands.ListInventory;
import dev.ebullient.pockets.commands.MoveInventory;
import dev.ebullient.pockets.commands.RemoveInventory;
import dev.ebullient.pockets.commands.ResetAll;
import dev.ebullient.pockets.commands.profile.ProfileCommand;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InputOutputOptionsMixin;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.IParameterExceptionHandler;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;
import picocli.CommandLine.UnmatchedArgumentException;

@QuarkusMain
@Command(name = "pockets", header = "What have you got in your pockets?",
        subcommands = {
                AddItem.class,
                CreateItem.class,
                BuyInventory.class,
                ListInventory.class,
                MoveInventory.class,
                RemoveInventory.class,
                ProfileCommand.class,
                ResetAll.class },
        footer = {
                "",
                BOOM + NBSP + "indicates destructive commands and options.",
                "",
                "To backup your configuration and data:",
                "  '${ROOT-COMMAND-NAME} profile export --data <profile name>'",
                "" },
        showDefaultValues = true,
        mixinStandardHelpOptions = true,
        sortOptions = false,
        headerHeading = "%n",
        synopsisHeading = "%nUsage: ",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "Options:%n",
        commandListHeading = "%nCommands:%n",
        scope = ScopeType.INHERIT)
public class PocketsCli implements Callable<Integer>, QuarkusApplication {
    @Spec
    Model.CommandSpec spec;

    @Inject
    IFactory factory;

    @Inject
    CamelMain camelMain;

    @Mixin
    InputOutputOptionsMixin ioOptions;

    // This is an alternate declaration of an option used for many item
    // commands that allows it to appear before or after the subcommand.
    // It is not inherited, and the subcommands show it in help (hidden here)
    @Option(names = { "-p", "--profile" }, scope = ScopeType.LOCAL, hidden = true)
    public String profileId;

    @Override
    public Integer call() throws Exception {
        // invocation of `pockets` command with no other command specified.
        // Point to the web ui and wait...

        Tui.println("",
                "🛍️  What have you got in your pockets?",
                "",
                "Known profiles: " + Profile.listByNaturalId(),
                "",
                "Visit the UI: http://localhost:" + System.getProperty("quarkus.http.port"),
                "",
                "⏱️  Waiting until you're done...   Use [Ctrl/Cmd-C] to cancel",
                "",
                "This is a locally running process that does not gather any information about you or your usage. Use --help for other commands and options.");

        // web-routes are only started in this case
        camelMain.getCamelContext().getRouteController().startAllRoutes();

        Quarkus.waitForExit();
        return ExitCode.OK;
    }

    private int executionStrategy(ParseResult parseResult) {
        try {
            init(parseResult);
            camelMain.startEngine();
            return new CommandLine.RunLast().execute(parseResult);
        } catch (Exception e) {
            Tui.errorf(e, "An error occurred", e.getMessage());
            return ExitCode.SOFTWARE;
        } finally {
            shutdown();
        }
    }

    private void init(ParseResult parseResult) {
        Tui.init(spec, ioOptions);
    }

    void onStop(@Observes ShutdownEvent ev) {
        Tui.debug("The application is stopping...");
    }

    private void shutdown() {
        Tui.close();
    }

    @Override
    @ActivateRequestContext
    public int run(String... args) throws Exception {
        CommandLine cmd = new CommandLine(this, factory)
                .setParameterExceptionHandler(new ShortErrorMessageHandler())
                .setCaseInsensitiveEnumValuesAllowed(true);

        return cmd.setExecutionStrategy(this::executionStrategy).execute(args);
    }

    static class ShortErrorMessageHandler implements IParameterExceptionHandler {

        public int handleParseException(ParameterException ex, String[] args) {
            CommandLine cmd = ex.getCommandLine();
            Model.CommandSpec spec = cmd.getCommandSpec();

            Tui.error(ex, ex.getMessage()); // bold red
            try (PrintWriter err = Tui.err()) {
                UnmatchedArgumentException.printSuggestions(ex, err);
                err.println(cmd.getHelp().fullSynopsis()); // normal text to error stream

                if (spec.equals(spec.root())) {
                    err.println(cmd.getHelp().commandList()); // normal text to error stream
                }
                err.printf("See '%s --help' for more information.%n", spec.qualifiedName());
                err.flush();
            }

            return cmd.getExitCodeExceptionMapper() != null ? cmd.getExitCodeExceptionMapper().getExitCode(ex)
                    : spec.exitCodeOnInvalidInput();
        }
    }
}
