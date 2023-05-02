package dev.ebullient.pockets.commands.profile;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.BOOM;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;
import static dev.ebullient.pockets.io.PocketsFormat.PROFILE;

import java.util.concurrent.Callable;

import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.commands.mixin.ActiveProfileMixinParameter;
import dev.ebullient.pockets.commands.mixin.TargetFilePathMixin;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;

@Command(name = "import", header = PROFILE + BOOM + NBSP + "Import configuration settings into profile.")
class ImportProfileData implements Callable<Integer> {
    @Inject
    ProfileContext config;

    @Mixin
    ActiveProfileMixinParameter activeProfileMixin;

    @Mixin
    TargetFilePathMixin pathMixin;

    @Override
    @Transactional
    public Integer call() {
        if (pathMixin.notReadable()) {
            return ExitCode.USAGE;
        }
        ProfileConfigData newCfg = pathMixin.readProfileConfig();
        if (newCfg == null) {
            return ExitCode.USAGE;
        }
        newCfg.validate(); // make sure defined reference types are sane

        ProfileConfigData oldCfg = activeProfileMixin.promptFindIfPresent(config, false,
                "Name of import target profile. This can be a new or existing profile name (default)");

        try {
            if (oldCfg == null) {
                newCfg.slug = activeProfileMixin.getId(); // keep caller's preferred name
                Profile.createProfile(newCfg);
            } else {
                newCfg.slug = oldCfg.slug;
                Profile.updateProfile(oldCfg, newCfg);
            }

            // TODO: --data ..
            Tui.donef("Settings have been imported into @|bold %s|@.%n", newCfg.slug);
        } catch (InvalidPocketState ips) {
            Tui.error(ips);
            return ips.getExitCode();
        } catch (PersistenceException pe) {
            Tui.errorf(pe, "Unable to import settings: %s", pe.getMessage());
            return ExitCode.USAGE;
        }
        return ExitCode.OK;
    }
}
