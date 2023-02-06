package dev.ebullient.pockets.commands.mixin;

import java.util.List;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.config.ProfileConfigData;
import picocli.CommandLine.Parameters;

/**
 * The profile parameter is used by Profile commands.
 * The profiles are being manipulated (rather than the things linked to them).
 * There is no default value (for example).
 */
public class ActiveProfileMixinParameter extends ActiveProfileMixinBase {
    protected String slugId;
    protected boolean specified = false;

    @Parameters(description = "Id of the target profile", arity = "0..*")
    void setNameOrId(List<String> words) {
        slugId = Transform.slugify(String.join("-", words));
    }

    public boolean specified() {
        return specified;
    }

    // List
    public ProfileConfigData findIfSpecified(ProfileContext config) {
        if (Transform.isBlank(slugId)) {
            return null; // nothing specified. Something other than optional (ok)
        }
        specified = true;
        return searchConfigDataById(slugId, true);
    }

    // Create
    public String getUniqueNameOrPrompt(ProfileContext config) {
        slugId = getRequiredIdOrPrompt("Name for the new profile", slugId, null);
        specified = !Transform.isBlank(slugId);
        return (Transform.isBlank(slugId) || slugIdInUse(slugId)) ? null : slugId;
    }

    // Import / Export / Remove
    public ProfileConfigData promptFindIfPresent(ProfileContext config, boolean required, String prompt) {
        slugId = getRequiredIdOrPrompt(prompt, slugId, "default");
        if (Transform.isBlank(slugId)) {
            return null;
        }
        specified = true;
        return searchConfigDataById(slugId, required);
    }

    public String getId() {
        return Transform.slugify(slugId);
    }
}
