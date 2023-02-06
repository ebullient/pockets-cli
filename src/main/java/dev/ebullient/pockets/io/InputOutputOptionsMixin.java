package dev.ebullient.pockets.io;

import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;

public class InputOutputOptionsMixin {

    @Option(names = { "--debug" }, description = "Enable debug output", scope = ScopeType.INHERIT)
    boolean debug = true; // TODO

    @Option(names = { "--quiet" }, description = "Enable quieter (less verbose) output", scope = ScopeType.INHERIT)
    boolean quiet;

    @Option(names = { "-i",
            "--interactive" }, description = "Confirm and prompt for missing arguments.", required = false, help = true,
            scope = ScopeType.INHERIT)
    boolean interactive = false;
}
