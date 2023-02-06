package dev.ebullient.pockets.config;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.config.EmitterConfig.EmitterType;
import dev.ebullient.pockets.config.EmitterConfig.EventLogConfiguration;
import dev.ebullient.pockets.config.EmitterConfig.MarkdownConfiguration;
import dev.ebullient.pockets.config.Types.CurrencyRef;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.config.Types.PocketRef;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.io.InvalidPocketState;
import dev.ebullient.pockets.io.PocketTui;

public class TypeConversion {

    public static class DeserializeItemRef extends StdDeserializer<ItemRef> {
        public DeserializeItemRef() {
            this(null);
        }

        public DeserializeItemRef(Class<ItemRef> vc) {
            super(vc);
        }

        @Override
        public ItemRef deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonStreamContext parsingContext = p.getParsingContext();
            JsonStreamContext parent = parsingContext.getParent();
            Modification currentValue = (Modification) parent.getCurrentValue();
            switch (currentValue.itemType) {
                case CURRENCY:
                    return p.readValueAs(CurrencyRef.class);
                case POCKET:
                    return p.readValueAs(PocketRef.class);
                default:
                    return p.readValueAs(ItemRef.class);
            }
        }

    }

    public static class DeserializeItemDetails extends StdDeserializer<ItemDetails> {
        public DeserializeItemDetails() {
            this(null);
        }

        public DeserializeItemDetails(Class<ItemDetails> vc) {
            super(vc);
        }

        @Override
        public ItemDetails deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonStreamContext parsingContext = p.getParsingContext();
            JsonStreamContext parent = parsingContext.getParent();
            Modification currentValue = (Modification) parent.getCurrentValue();
            if (Objects.requireNonNull(currentValue.itemType) == ItemType.POCKET) {
                return p.readValueAs(PocketDetails.class);
            }
            return p.readValueAs(ItemDetails.class);
        }
    }

    public static class DeserializeItemClass extends StdDeserializer<Object> {
        public DeserializeItemClass() {
            this(null);
        }

        public DeserializeItemClass(Class<Object> vc) {
            super(vc);
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonStreamContext parsingContext = p.getParsingContext();
            JsonStreamContext parent = parsingContext.getParent();

            Posting currentValue = (Posting) parent.getCurrentValue();
            if (Objects.requireNonNull(currentValue.itemType) == ItemType.POCKET) {
                return p.readValueAs(Pocket.class);
            }
            return p.readValueAs(Item.class);
        }
    }

    public static class SerializeMapToList<T> extends StdSerializer<Map<String, T>> {
        public SerializeMapToList() {
            this(null);
        }

        public SerializeMapToList(Class<Map<String, T>> t) {
            super(t);
        }

        @Override
        public void serialize(Map<String, T> map, JsonGenerator gen, SerializerProvider provider) throws IOException {
            provider.defaultSerializeValue(map.values(), gen);
        }
    }

    public abstract static class DeserializeListToMap<T extends ItemRef> extends StdDeserializer<Map<String, T>> {
        public DeserializeListToMap() {
            this(null);
        }

        public DeserializeListToMap(Class<Map<String, T>> vc) {
            super(vc);
        }

        @Override
        public Map<String, T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isArray()) {
                Map<String, T> map = new HashMap<>();
                for (JsonNode x : Transform.toIterable(node.elements())) {
                    try {
                        T value = getValue(p, x);
                        String key = getKey(value);
                        value.id = key;
                        T old = map.put(key, value);
                        if (old != null) {
                            throw new InvalidPocketState(PocketTui.BAD_DATA,
                                    "Duplicate key %s found while deserializing configuration data: %s",
                                    key, x);
                        }
                    } catch (JsonProcessingException e) {
                        throw new InvalidPocketState(e, PocketTui.BAD_DATA, "Unable to deserialize configuration data: %s", x);
                    }
                }
                return map;
            }
            return null;
        }

        protected String getKey(T value) {
            if (value.id == null) {
                value.id = Transform.slugify(value.name);
            }
            return value.id;
        }

        protected abstract T getValue(JsonParser p, JsonNode x) throws JsonProcessingException;
    }

    public static class DeserializeCurrencyListToMap extends DeserializeListToMap<CurrencyRef> {

        protected String getKey(CurrencyRef value) {
            if (value.id == null) {
                value.id = value.notation;
            }
            return value.id;
        }

        protected CurrencyRef getValue(JsonParser p, JsonNode x) throws JsonProcessingException {
            return p.getCodec().treeToValue(x, CurrencyRef.class);
        }
    }

    public static class DeserializeItemListToMap extends DeserializeListToMap<ItemRef> {
        protected ItemRef getValue(JsonParser p, JsonNode x) throws JsonProcessingException {
            return p.getCodec().treeToValue(x, ItemRef.class);
        }
    }

    public static class DeserializePocketListToMap extends DeserializeListToMap<PocketRef> {
        protected PocketRef getValue(JsonParser p, JsonNode x) throws JsonProcessingException {
            return p.getCodec().treeToValue(x, PocketRef.class);
        }
    }

    public static class DeserializeEmitterListToMap extends StdDeserializer<Map<EmitterType, EmitterConfig<?>>> {
        public DeserializeEmitterListToMap() {
            this(null);
        }

        public DeserializeEmitterListToMap(Class<Map<EmitterType, EmitterConfig<?>>> vc) {
            super(vc);
        }

        @Override
        public Map<EmitterType, EmitterConfig<?>> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isArray()) {
                Map<EmitterType, EmitterConfig<?>> map = new HashMap<>();
                for (JsonNode x : Transform.toIterable(node.elements())) {
                    try {
                        EmitterType key = Optional.ofNullable(x.get("type"))
                                .map(t -> EmitterConfig.EmitterType.valueOf(t.asText()))
                                .orElse(EmitterConfig.EmitterType.unknown);
                        EmitterConfig<?> value = null;
                        switch (key) {
                            case markdown:
                                value = p.getCodec().treeToValue(x, MarkdownConfiguration.class);
                                break;
                            case eventLog:
                                value = p.getCodec().treeToValue(x, EventLogConfiguration.class);
                                break;
                            default:
                                Tui.errorf("Unknown Emitter type %s, ignoring", key);
                                break;
                        }
                        if (value == null) {
                            throw new InvalidPocketState(PocketTui.BAD_DATA,
                                    "Unable to deserialize configuration data (unknown type): %s",
                                    key, x);
                        }

                        EmitterConfig<?> old = map.put(key, value);
                        if (old != null) {
                            throw new InvalidPocketState(PocketTui.BAD_DATA,
                                    "Duplicate key %s found while deserializing configuration data: %s",
                                    key, x);
                        }
                    } catch (JsonProcessingException e) {
                        throw new InvalidPocketState(e, PocketTui.BAD_DATA, "Unable to deserialize configuration data: %s", x);
                    }
                }
                return map;
            }
            return null;
        }
    }
}
