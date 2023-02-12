package dev.ebullient.pockets.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.control.ActivateRequestContext;

import org.jboss.logging.Logger;

import dev.ebullient.pockets.db.ProfileConfigEntity;
import io.quarkus.logging.Log;

public class PocketsConfig {
    public final static Path USER_HOME = Paths.get(System.getProperty("user.home"));
    public final static Path USER_DIR = Paths.get(System.getProperty("user.dir"));
    final static Logger LOG = Logger.getLogger("pockets");

    String jdbcUrlBase;
    Path configPath;
    String activeProfile;
    Set<String> profileNames = new HashSet<>();
    Map<String, ProfileConfig> profiles = new HashMap<>();

    @ActivateRequestContext
    public void init(String configDir, String profile) {
        if (configPath != null) {
            return; // configure only once
        }

        this.activeProfile = profile == null ? "default" : profile;

        if (configDir == null) {
            this.jdbcUrlBase = "jdbc:h2:~/.pockets";
            this.configPath = USER_HOME.resolve(".pockets");
        } else {
            configDir = configDir.replace("\\", "/");
            Path p = configDir.startsWith("~/")
                    ? USER_HOME.resolve(configDir.replace("~/", "")).normalize()
                    : USER_DIR.resolve(configDir).normalize();

            File f = p.toFile();
            if (f.exists() && !f.isDirectory()) {
                throw new IllegalArgumentException("Specified configuration directory exists and is a file: " + configDir);
            }

            this.configPath = p;
            this.jdbcUrlBase = configDir.startsWith("~/") ? configDir : p.toString();
        }

        configPath.toFile().mkdirs();
        try {
            Files.find(configPath, 1, (path, attributes) -> {
                File f = path.toFile();
                return f.isFile() && f.getName().matches(".*\\.db\\.*");
            }).forEach(p -> {
                String filename = p.toFile().getName();
                String profileName = filename.replace("(.*)\\.db\\.*", "$1");
                profileNames.add(profileName);

                if (profileName.equalsIgnoreCase(activeProfile)) {
                    ProfileConfig profileConfig = ProfileConfigEntity.findByName(profileName);
                    Log.debugf("Loaded config data for profile %s: %s", profileName, profileConfig);
                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to determine available profiles in " + configDir);
        }
        LOG.debugf("Using profile %s from config directory: %s (%s)", profile, configDir, profiles);
    }

    public Collection<String> getProfiles() {
        return profileNames;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public String getJdbcUrlBase() {
        return jdbcUrlBase;
    }

    @Override
    public String toString() {
        return "PocketsConfig [jdbcUrlBase=" + jdbcUrlBase
                + ", activeProfile=" + activeProfile
                + ", configPath=" + configPath + "]";
    }
}
