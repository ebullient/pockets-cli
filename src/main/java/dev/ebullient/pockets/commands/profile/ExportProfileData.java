package dev.ebullient.pockets.commands.profile;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;
import static dev.ebullient.pockets.io.PocketsFormat.PROFILE;
import static dev.ebullient.pockets.io.PocketsFormat.SAVE;

import java.util.concurrent.Callable;

import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.commands.mixin.ActiveProfileMixinParameter;
import dev.ebullient.pockets.commands.mixin.TargetFilePathMixin;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.io.PocketTui;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "export", header = PROFILE + SAVE + NBSP + "Export profile settings.")
class ExportProfileData implements Callable<Integer> {
    @Inject
    ProfileContext config;

    @Mixin
    ActiveProfileMixinParameter activeProfileMixin;

    @Mixin
    TargetFilePathMixin pathMixin;

    @Option(names = { "-f", "--force" }, description = "Overwrite an existing file", defaultValue = "false")
    boolean overwrite;

    @Override
    @Transactional
    public Integer call() {
        ProfileConfigData pc = activeProfileMixin.promptFindIfPresent(config, true,
                "Name of the profile to export (default)");

        if (pc == null) {
            return activeProfileMixin.specified() ? PocketTui.NOT_FOUND : ExitCode.USAGE;
        }
        if (pathMixin.notWritable(overwrite)) {
            return ExitCode.USAGE;
        }

        int result = pathMixin.writeProfileConfigValue(pc);
        // TODO: --data ..
        if (result == ExitCode.OK) {
            Tui.donef("Settings have been exported from @|bold %s|@.%n", pc.slug);
        }
        return result;
    }
}
