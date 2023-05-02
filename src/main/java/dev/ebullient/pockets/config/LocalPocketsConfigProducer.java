package dev.ebullient.pockets.config;

import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketTui;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class LocalPocketsConfigProducer {
    @ConfigProperty(name = "pockets.directory")
    Path pocketsDirectory;

    @Produces
    @ApplicationScoped
    public LocalPocketsConfig readConfiguration() {
        Path configYaml = pocketsDirectory.resolve("config.yaml");
        Path configJson = pocketsDirectory.resolve("config.json");
        Path configFile = configYaml.toFile().exists() ? configYaml : configJson;

        LocalPocketsConfig config = null;
        if (configFile.toFile().exists()) {
            ObjectMapper mapper = Transform.mapper(configFile);
            try {
                config = mapper.readValue(configFile.toFile(), LocalPocketsConfig.class);
            } catch (IOException e) {
                throw new InvalidPocketState(e, PocketTui.BAD_DATA, "Unable to read local configuration %s", configFile);
            }
        } else {
            config = new LocalPocketsConfig();
        }
        config.setDefaults(pocketsDirectory);
        return config;
    }
}
