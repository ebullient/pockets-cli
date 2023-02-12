package dev.ebullient.pockets.config;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.Types.WrappedIOException;
import io.quarkus.logging.Log;

public class TypeConversion {

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

    public static class DeserializeListToMap<T> extends StdDeserializer<Map<String, T>> {

        private Class<T> typeOfT;

        protected DeserializeListToMap() {
            this(null);
        }

        @SuppressWarnings({ "unchecked" })
        protected DeserializeListToMap(Class<?> vc) {
            super(vc);
            this.typeOfT = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        }

        @Override
        public Map<String, T> deserialize(JsonParser p, DeserializationContext ctxt)
                throws IOException, JacksonException {
            JsonNode node = p.getCodec().readTree(p);
            if (node.isArray()) {
                Map<String, T> result = new HashMap<>();
                node.forEach(x -> {
                    String name = x.get("name").asText();
                    try {
                        T obj = p.getCodec().treeToValue(x, typeOfT);
                        result.put(Transform.slugify(name), obj);
                    } catch (JsonProcessingException e) {
                        Log.debugf("Unable to read preset data", e.getMessage());
                        throw new WrappedIOException(e);
                    }
                });
                return result;
            }
            return null;
        }
    }
}
