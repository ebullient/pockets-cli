package dev.ebullient.pockets.config;

import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dev.ebullient.pockets.config.Types.Currency;
import dev.ebullient.pockets.config.Types.Item;
import dev.ebullient.pockets.config.Types.Pocket;
import dev.ebullient.pockets.config.Types.PresetCapacityType;

public class PresetValues {

    String name;
    PresetCapacityType capacityType;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeListToMap.class)
    Map<String, Currency> currency;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeListToMap.class)
    Map<String, Pocket> pockets;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeListToMap.class)
    Map<String, Item> items;

    @Override
    public String toString() {
        return name + " (type=" + capacityType + ", currency=" + currency.size() + ", pockets="
                + pockets.size() + ", items=" + items.size() + ")";
    }
}
