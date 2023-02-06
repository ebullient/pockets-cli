package dev.ebullient.pockets.commands.profile;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.BOOM;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;
import static dev.ebullient.pockets.io.PocketsFormat.PROFILE;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.commands.mixin.ActiveProfileMixinParameter;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;

@Command(name = "remove", header = PROFILE + BOOM + NBSP + "Remove a config profile")
class RemoveProfileData implements Callable<Integer> {
    @Inject
    ProfileContext config;

    @Mixin
    ActiveProfileMixinParameter activeProfileMixin;

    @Override
    @Transactional
    public Integer call() {
        ProfileConfigData pc = activeProfileMixin.promptFindIfPresent(config, true,
                "Name of the profile to remove (default)");
        if (pc == null) {
            return activeProfileMixin.specified() ? PocketTui.NOT_FOUND : ExitCode.USAGE;
        }

        if (!Tui.confirm("Do you want to remove this profile", true)) {
            Tui.infof("Profile @|bold %s|@ was not removed.%n", pc.slug);
        } else {
            Profile.deleteByNaturalId(pc.slug);
            Tui.donef("Profile @|bold %s|@ has been %s.%n", pc.slug,
                    "default".equals(pc.slug) ? "reset" : "removed");

            if (Tui.isVerboseEnabled()) {
                activeProfileMixin.showProfiles();
            }
        }
        return ExitCode.OK;
    }
}
