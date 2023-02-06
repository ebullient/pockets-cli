package dev.ebullient.pockets.commands.mixin;

import java.util.Optional;

import dev.ebullient.pockets.PocketsCli;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.ScopeType;

/**
 * The profile Option is used by most commands, and determines
 * the profile that should be used for the duration..
 */
public class ActiveProfileMixinOption extends ActiveProfileMixinBase {

    @ParentCommand
    PocketsCli parent;

    @ArgGroup(heading = "%nSpecify target profile%n", exclusive = false)
    ProfileOption option = new ProfileOption();

    public Optional<String> findActiveProfileId() {
        option.slugId = option.slugId == null ? parent.profileId : option.slugId;
        option.slugId = getRequiredIdOrPrompt("Name of the profile to export (default)", option.slugId, "default");
        return Optional.ofNullable(option.slugId);
    }

    static class ProfileOption {
        @Option(order = 0, names = { "-p", "--profile" }, scope = ScopeType.INHERIT,
                description = "Active profile (required)%n  Default: default")
        protected String slugId;
    }
}
