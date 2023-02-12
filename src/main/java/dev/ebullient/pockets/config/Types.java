package dev.ebullient.pockets.config;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface Types {

    public enum PresetFlavor {
        config5e,
        configPf2e;
    }

    public enum PresetCapacityType {
        bulk,
        weight;
    }

    public static class Compartment {
        public String constraint;
        public double max_weight;
        public double max_volume;
        public String max_bulk;
    }

    public static class Currency {
        public String notation;
        public String name;
        public double unitConversion;
    }

    public static class Item {
        public String name;
        public String category;
        public String rarity;

        public String value;
        public Integer quantity;

        public double weight;
        public String bulk;
    }

    public static class Pocket extends Item {
        public String emoji;
        public boolean extradimensional;
        public List<Compartment> compartments;
    }

    public static class ConfigProfile {
        public String name;
        public PresetFlavor preset;
        public PresetCapacityType capacityType;
        public Collection<Currency> currency;
        public Collection<Pocket> pockets;
    }

    // IOException -> RuntimeException .. for working w/in stream/function
    public static class WrappedIOException extends RuntimeException {
        public WrappedIOException(IOException cause) {
            super(cause);
        }
    }
}
