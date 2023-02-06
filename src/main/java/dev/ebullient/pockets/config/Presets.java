package dev.ebullient.pockets.config;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.LambdaRouteBuilder;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.routes.WebRoutes;

// @JsonDeserialize(using = Presets.Deserializer.class)
public class Presets {

    private static Presets instance = new Presets();

    /**
     * Used indirectly via rest
     *
     * @return Presets read from the classpath
     * @see WebRoutes#addRoutes(CamelContext, LambdaRouteBuilder)
     */
    public static PresetValues getPresets(PresetFlavor flavor) {
        PresetValues presetValues = instance.presets.get(flavor);
        if (presetValues == null) {
            try (InputStream is = Presets.class
                    .getResourceAsStream("/META-INF/resources/config/preset-" + flavor.name() + ".json")) {
                presetValues = Transform.JSON.readValue(is, PresetValues.class);
                instance.presets.put(flavor, presetValues);
                Tui.debugf("Fetched presets for %s: %s", flavor, presetValues);
            } catch (IOException e) {
                throw new InvalidPocketState(e, "Unable to read preset data for %s", flavor, e.getMessage());
            }
        }
        return presetValues;
    }

    private Map<Types.PresetFlavor, PresetValues> presets = new HashMap<>();

    @Override
    public String toString() {
        return "Presets " + presets.values();
    }
}
