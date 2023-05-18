package dev.ebullient.pockets.routes;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.apache.camel.Exchange;
import org.apache.camel.Message;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.pockets.actions.ModificationRequest;
import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.config.PresetValues;
import dev.ebullient.pockets.config.Presets;
import dev.ebullient.pockets.config.Types.PocketConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.db.Profile;

@Dependent
public class WebRouteHandler {
    @Inject
    ProfileContext profileContext;

    @Inject
    ModifyPockets modifyPockets;

    /** Web-driven config fetch */
    @Transactional
    public PocketConfigData fetchConfigData() {
        PocketConfigData data = new PocketConfigData();
        data.profiles = Profile.listAllProfiles();
        data.activeProfile = "default";
        return data;
    }

    /** Web-driven Reset. */
    @Transactional
    public PocketConfigData updateConfigData(PocketConfigData updates) {
        // preserve active profile for web ui if possible
        String activeProfileKey = Profile.bulkUpdate(updates.activeProfile, updates.profiles);
        // get refreshed profiles; will always contain default profile
        updates.profiles = Profile.listAllProfiles();
        updates.activeProfile = activeProfileKey;
        return updates;
    }

    @SuppressWarnings("unused")
    public PresetValues getPreset(PresetFlavor preset) {
        return Presets.getPresets(preset);
    }

    @Transactional
    public List<String> getProfiles() {
        return Profile.listByNaturalId();
    }

    @Transactional
    public void validateProfile(Exchange exchange) {
        final String profileKey = "profile";

        Message message = exchange.getMessage();
        String profile = (String) message.getHeader(profileKey);

        // Update header with full name, may throw
        profile = ProfileContext.validateProfile(profile);

        message.setHeader(profileKey, profile);
    }

    @Transactional
    public JsonNode getProfile(String profileName) {
        profileContext.setActiveProfile(profileName);
        return profileContext.getActiveProfile().export();
    }

    @Transactional
    public ModificationResponse doModification(ModificationRequest modificationRequest, String profileName) {
        profileContext.setActiveProfile(profileName);
        return modifyPockets.modifyPockets(modificationRequest);
    }
}
