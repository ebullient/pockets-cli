package dev.ebullient.pockets.actions;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.List;

import jakarta.transaction.TransactionScoped;

import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.db.Profile.ProfileLookupKeys;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketTui;
import dev.ebullient.pockets.io.PocketsFormat;

@TransactionScoped
public class ProfileContext {

    private ProfileLookupKeys activeKeys;

    public static String validateProfile(String profileName) {
        List<String> matchingProfile = Profile.listByNaturalIdMatching(profileName);
        if (matchingProfile.size() > 1) {
            throw new InvalidPocketState(PocketTui.CONFLICT,
                    PocketsFormat.MATCHES_MANY, profileName,
                    "profile: " + String.join(", ", matchingProfile));
        } else if (matchingProfile.isEmpty()) {
            List<String> profiles = Profile.listByNaturalId();
            throw new InvalidPocketState(PocketTui.NOT_FOUND, PocketsFormat.DOES_NOT_MATCH + " Known profiles: %s",
                    profileName, "any known profiles.", String.join(", ", profiles));
        }
        return matchingProfile.get(0);
    }

    /** Profile command: active profile option */
    public void setActiveProfile(String slugId) {
        activeKeys = Profile.findKeysByNaturalId(slugId);
        Tui.infof("Using profile: %s", activeKeys.slug);
    }

    public Profile getActiveProfile() {
        if (activeKeys == null) {
            throw new InvalidPocketState(true, "Must specify a profile for the session");
        }
        return Profile.findById(activeKeys.id);
    }

    public String getActiveProfileName() {
        return activeKeys.slug;
    }
}
