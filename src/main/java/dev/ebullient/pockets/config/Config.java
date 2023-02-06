package dev.ebullient.pockets.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

public class Config {
    public final static ObjectMapper FROM_JSON = new ObjectMapper()
            .setVisibility(VisibilityChecker.Std.defaultInstance().with(JsonAutoDetect.Visibility.ANY));

    enum CapacityType {
        bulk,
        weight;
    }
}
