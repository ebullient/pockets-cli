package dev.ebullient.pockets.commands.profile;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.*;

import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.transaction.Transactional;

import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.commands.mixin.ActiveProfileMixinParameter;
import dev.ebullient.pockets.commands.profile.ProfileCommand.PresetCandidates;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.db.Profile;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = "create", header = PROFILE + CREATE + NBSP + "Create a config profile")
class CreateProfile implements Callable<Integer> {
    @Inject
    ProfileContext config;

    @Mixin
    ActiveProfileMixinParameter activeProfileMixin;

    @Option(names = { "-p", "--preset" }, required = true,
            defaultValue = "dnd5e",
            completionCandidates = PresetCandidates.class,
            description = "Ruleset / defaults for this pocket.%n  Supported values: ${COMPLETION-CANDIDATES}.")
    PresetFlavor preset;

    @Option(names = { "--desc" }, description = "Description of profile. Will be set")
    String description;

    @Override
    @Transactional
    public Integer call() throws Exception {
        String nameOrId = activeProfileMixin.getUniqueNameOrPrompt(config);
        if (nameOrId == null) {
            return ExitCode.USAGE;
        }

        ProfileConfigData pc = ProfileConfigData.create(nameOrId);
        pc.preset = preset; // has default, always set
        if (description != null) {
            pc.description = description;
        }

        Profile.createProfile(pc); // save config
        if (Tui.isVerboseEnabled()) {
            Tui.createf("Profile %s created.", pc.slug);
            ProfileCommand.describeProfile(pc);
        }
        return ExitCode.OK;
    }
}
