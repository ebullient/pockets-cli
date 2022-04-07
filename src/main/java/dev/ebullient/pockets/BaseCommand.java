package dev.ebullient.pockets;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

public class BaseCommand implements Callable<Integer> {

    @Spec
    CommandSpec spec;

    @Inject
    PocketTui tui;

    @Override
    public Integer call() throws Exception {
        tui.showUsage(spec);
        return ExitCode.OK;
    }
}
