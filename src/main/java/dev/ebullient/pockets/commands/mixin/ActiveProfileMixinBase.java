package dev.ebullient.pockets.commands.mixin;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.Optional;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketsFormat;

public class ActiveProfileMixinBase {
    // Choose a variant instead: Option or Parameter
    protected ActiveProfileMixinBase() {
    }

    public void showProfiles() {
        PocketsFormat.showProfiles(Profile.listAllProfiles());
    }

    protected String getRequiredIdOrPrompt(String prompt,
            String slugId, String defaultValue) {
        if (Tui.interactive() && (slugId == null || slugId.isBlank())) {
            showProfiles();
            slugId = Tui.promptIfMissing(prompt, slugId);
        }
        if (Transform.isBlank(slugId)) {
            slugId = defaultValue;
            if (Transform.isBlank(slugId)) {
                Tui.warn("Profile id is required.");
            }
        }
        return slugId;
    }

    protected boolean slugIdInUse(String slugId) {
        Optional<String> profileName = searchByNaturalId(slugId, false);
        return profileName.isPresent();
    }

    protected ProfileConfigData searchConfigDataById(String slugId, boolean required) {
        Optional<String> profileName = searchByNaturalId(slugId, required);
        if (profileName.isPresent()) {
            Profile p = Profile.findByNaturalId(profileName.get());
            return p.config;
        }
        return null;
    }

    protected Optional<String> searchByNaturalId(String slugId, boolean required) {
        try {
            return Optional.of(ProfileContext.validateProfile(slugId));
        } catch (InvalidPocketState ips) {
            if (required) {
                Tui.error(ips);
            }
            return Optional.empty();
        }
    }
}
