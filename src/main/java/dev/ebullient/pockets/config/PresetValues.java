package dev.ebullient.pockets.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.Types.CurrencyRef;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.config.Types.PocketRef;
import dev.ebullient.pockets.config.Types.PresetCapacityType;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PresetValues {
    String name;
    PresetCapacityType capacityType;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeCurrencyListToMap.class)
    Map<String, CurrencyRef> currency = new HashMap<>();

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializePocketListToMap.class)
    Map<String, PocketRef> pocketRef = new HashMap<>();

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeItemListToMap.class)
    Map<String, ItemRef> itemRef = new HashMap<>();

    @Override
    public String toString() {
        return name + "[type=" + capacityType
                + ", currency=" + Transform.sizeOrNull(currency)
                + ", pockets=" + Transform.sizeOrNull(pocketRef)
                + ", items=" + Transform.sizeOrNull(itemRef)
                + "]";
    }

    public String getName() {
        return name;
    }

    public PresetCapacityType getCapacityType() {
        return capacityType;
    }

    public Map<String, CurrencyRef> getCurrency() {
        return currency;
    }

    public Map<String, PocketRef> getPocketRef() {
        return pocketRef;
    }

    public Map<String, ItemRef> getItemRef() {
        return itemRef;
    }
}
