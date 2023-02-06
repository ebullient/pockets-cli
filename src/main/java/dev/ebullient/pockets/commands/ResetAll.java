package dev.ebullient.pockets.commands;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.BOOM;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.PocketsFormat;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;

@Command(name = "reset",
        header = BOOM + NBSP + "Reset everything.")
public class ResetAll implements Callable<Integer> {
    @Inject
    ProfileContext config;

    @Override
    @Transactional
    public Integer call() throws Exception {
        if (Tui.interactive()) {
            PocketsFormat.showProfiles(Profile.listAllProfiles());
            if (!Tui.confirm("Reset all profiles and associated data", true)) {
                Tui.info("Configuration was not reset.");
                return ExitCode.OK;
            }
        }
        Profile.bulkUpdate(null, new HashMap<>());
        Tui.done("All data has been reset.");
        if (Tui.isVerboseEnabled()) {
            PocketsFormat.showProfiles(Profile.listAllProfiles());
        }
        return ExitCode.OK;
    }
}
