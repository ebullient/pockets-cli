package dev.ebullient.pockets.reference;

import java.io.File;
import java.nio.file.Path;
import java.util.concurrent.Callable;

import dev.ebullient.pockets.Constants;
import dev.ebullient.pockets.Term;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@Command(name = "import", header = "Import reference items and pockets", subcommands = {
        Convert5eTools.class
}, footer = {
        "%n%nItems from the SRD are already included in pockets by default.",
        "You can add your own items to pockets by creating an index.json file",
        "in the pockets configuration directory (~/.pockets by default).",
        "The file should contain something like the following: ",
        Constants.JSON_EXAMPLE,
        "Please note:",
        "- Both the \"items\" and \"pockets\" elements should be defined",
        "  though they may be empty.",
        "- The key used to define an object should be a slugified version",
        "  of the item's name: all lowercase with special characters removed",
        "  and spaces replaced with '-'.",
        "- You can use the following schema to validate your index:",
        "  " + Constants.JSON_SCHEMA
})
public class Import implements Callable<Integer> {
    Path output;

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

    Path getOutputPath() {
        return output;
    }

    @Override
    public Integer call() throws Exception {
        Term.showUsage(spec);
        return ExitCode.OK;
    }
}
