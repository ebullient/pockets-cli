package dev.ebullient.pockets.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dev.ebullient.pockets.config.EmitterConfig.EmitterType;

public class LocalConfig {
    protected Path directory;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeEmitterListToMap.class)
    protected Map<EmitterType, EmitterConfig<?>> emitters = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected <T extends EmitterConfig<?>> T emitterFor(EmitterType type) {
        return (T) emitters.get(type);
    }
}
