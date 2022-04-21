package dev.ebullient.pockets;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import dev.ebullient.pockets.index.IndexConstants;
import dev.ebullient.pockets.index.Import5eTools;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@Command(name = "import", header = "Import reference items and pockets", subcommands = {
        Import5eTools.class
}, footer = {
        "%n",
        "Items from the SRD are already included in pockets by default. You can add",
        "your own items to pockets by creating an index.json file in the pockets",
        "configuration directory (~/.pockets by default). The file should contain ",
        "something like the following:%n",
        IndexConstants.JSON_EXAMPLE,
        "Please note:%n",
        "- Both the \"items\" and \"pockets\" elements should be defined.%n",
        "- The key used to define an object should be a slugified version of the item's",
        "  name: all lowercase; remove special characters; replace spaces with '-'.%n",
        "- You can use the following schema to validate your index:",
        "  " + IndexConstants.JSON_SCHEMA,
        "%n"
})
public class PocketsImport implements Callable<Integer> {
    Path output;

    @Inject
    PocketTui tui;

    @Spec
    CommandSpec spec;

    @Option(names = "-o", description = "Output directory", required = true, scope = ScopeType.INHERIT)
    void setOutputPath(File outputDir) {
        if (outputDir.exists() && outputDir.isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + output.toString());
        }
        outputDir.mkdirs();
        output = outputDir.toPath().toAbsolutePath().normalize();
    }

    public Path getOutputPath() {
        return output;
    }

    @Override
    public Integer call() throws Exception {
        tui.showUsage(spec);
        return ExitCode.OK;
    }
}
