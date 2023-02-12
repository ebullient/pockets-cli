package dev.ebullient.pockets.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jdk8.WrappedIOException;

import dev.ebullient.pockets.Transform;

@JsonDeserialize(using = Presets.Deserializer.class)
public class Presets {

    private static Presets instance;

    public static Presets getPresets() {
        Presets presets = instance;
        if (presets == null) {
            try (InputStream is = Presets.class.getResourceAsStream("/META-INF/resources/config/presets.json")) {
                presets = instance = Transform.FROM_JSON.readValue(is, Presets.class);
                PocketsConfig.LOG.debugf("Presets: %s", presets);
            } catch (IOException e) {
                PocketsConfig.LOG.errorf("Unable to read preset data", e.getMessage());
                throw new RuntimeException("Error reading presets.json: " + e.getMessage());
            }
        }
        return presets;
    }

    private Map<Types.PresetFlavor, PresetValues> presets;

    private Presets(Map<Types.PresetFlavor, PresetValues> presets) {
        this.presets = presets;
    }

    @Override
    public String toString() {
        return "Presets " + presets.values();
    }

    public static class Deserializer extends StdDeserializer<Presets> {

        public Deserializer() {
            this(null);
        }

        protected Deserializer(Class<Presets> vc) {
            super(vc);
        }

        @Override
        public Presets deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isObject()) {
                Map<Types.PresetFlavor, PresetValues> presets = new HashMap<>();
                node.fields().forEachRemaining(e -> {
                    try {
                        Types.PresetFlavor name = Types.PresetFlavor.valueOf(e.getKey());
                        PresetValues preset = p.getCodec().treeToValue(e.getValue(), PresetValues.class);
                        presets.put(name, preset);
                    } catch (JsonProcessingException jpe) {
                        PocketsConfig.LOG.errorf("Unable to read preset data", jpe.getMessage());
                        throw new WrappedIOException(jpe);
                    }
                });
                return new Presets(presets);
            }
            PocketsConfig.LOG.errorf("Unrecognized preset data", node.toString());
            throw new IllegalStateException("Unrecognized preset data");
        }
    }
}
