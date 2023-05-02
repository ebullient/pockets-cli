package dev.ebullient.pockets.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import dev.ebullient.pockets.config.EmitterConfig.EmitterType;
import dev.ebullient.pockets.config.EmitterConfig.EventLogConfiguration;
import dev.ebullient.pockets.config.EmitterConfig.MarkdownConfiguration;
import dev.ebullient.pockets.io.InvalidPocketState;

public class LocalPocketsConfig extends LocalConfig {

    protected Map<String, LocalConfig> profiles = new HashMap<>();

    /** Called just after reading config file **/
    public void setDefaults(Path pocketsDirectory) {
        directory = directory == null
                ? pocketsDirectory.resolve("output")
                : directory;
        emitters.values().forEach(e -> {
            e.setDefaults(this, null, null);
            e.enabled = e.enabled == null ? true : e.enabled; // enabled by default if present
        });
        profiles.forEach((k, v) -> {
            v.emitters.values().forEach(e -> {
                e.setDefaults(this, findEmitterConfig(k, e.getType()), k);
                e.enabled = e.enabled == null ? true : e.enabled; // enabled by default if present
            });
        });
    }

    public boolean jsonEventLogEnabled(String profileName) {
        return isEnabled(profileName, EmitterType.eventLog);
    }

    public boolean markdownEnabled(String profileName) {
        return isEnabled(profileName, EmitterType.markdown);
    }

    public EventLogConfiguration eventLogConfig(String profileName) {
        return findEmitterConfig(profileName, EmitterType.eventLog);
    }

    public MarkdownConfiguration markdownConfig(String profileName) {
        return findEmitterConfig(profileName, EmitterType.markdown);
    }

    private <T extends EmitterConfig<?>> T findEmitterConfig(String profileName, EmitterType type) {
        LocalConfig config = profiles.get(profileName);
        if (config == null) {
            return getDefaultEmitter(type);
        } else {
            T emitter = config.emitterFor(type);
            return emitter == null
                    ? getDefaultEmitter(type)
                    : emitter;
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends EmitterConfig<?>> T getDefaultEmitter(EmitterType type) {
        T emitter = emitterFor(type);
        if (emitter == null) {
            switch (type) {
                case markdown:
                    emitter = (T) new MarkdownConfiguration();
                    break;
                case eventLog:
                    emitter = (T) new EventLogConfiguration();
                    break;
                case unknown:
                    throw new InvalidPocketState("Cannot create default configuration for unknown emitter type %s", type);
            }
            emitter.setDefaults(this, null, null);
            emitters.put(type, emitter);
        }
        return emitter;
    }

    private boolean isEnabled(String profileName, EmitterType type) {
        EmitterConfig<?> emitterConfig = findEmitterConfig(profileName, type);
        if (emitterConfig != null) {
            return emitterConfig.getEnabled();
        }
        return false;
    }

    @Override
    public String toString() {
        return "LocalPocketsConfig{" +
                "directory=" + directory +
                ", emitters=" + emitters +
                '}';
    }
}
