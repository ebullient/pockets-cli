package dev.ebullient.pockets.commands.profile;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;
import static dev.ebullient.pockets.io.PocketsFormat.PROFILE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.commands.mixin.ActiveProfileMixinParameter;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.io.PocketTui;
import dev.ebullient.pockets.io.PocketsFormat;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;

@Command(name = "profile",
        header = PROFILE + NBSP + "Add, update, or delete a config profile.",
        subcommands = {
                CreateProfile.class,
                ImportProfileData.class,
                ExportProfileData.class,
                RemoveProfileData.class,
                GeneratePresets.class
        })
public class ProfileCommand implements Callable<Integer> {
    static class PresetCandidates extends ArrayList<String> {
        PresetCandidates() {
            super(List.of(PresetFlavor.dnd5e.name(), PresetFlavor.pf2e.name()));
        }
    }

    static void describeProfile(ProfileConfigData pc) {
        Tui.println(PocketsFormat.PROFILE_PREFIX + String.format("Profile @|bold %s|@%s.%s",
                pc.slug,
                pc.preset == null ? " does not have a preset defined" : " uses preset " + pc.preset,
                pc.description == null ? "" : "\n" + pc.description));
    }

    @Inject
    ProfileContext config;

    @Mixin
    ActiveProfileMixinParameter activeProfileMixin;

    @Override
    public Integer call() {
        ProfileConfigData profile = activeProfileMixin.findIfSpecified(config);
        if (profile == null) {
            activeProfileMixin.showProfiles();
            if (activeProfileMixin.specified()) {
                return PocketTui.NOT_FOUND;
            }
        } else {
            describeProfile(profile);
        }
        return ExitCode.OK;
    }
}
