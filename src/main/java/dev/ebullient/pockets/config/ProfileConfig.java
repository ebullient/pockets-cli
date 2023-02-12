package dev.ebullient.pockets.config;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dev.ebullient.pockets.config.Types.Currency;
import dev.ebullient.pockets.config.Types.Item;
import dev.ebullient.pockets.config.Types.Pocket;
import dev.ebullient.pockets.config.Types.PresetCapacityType;
import dev.ebullient.pockets.config.Types.PresetFlavor;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProfileConfig {
    public String name;
    public PresetFlavor preset;
    public PresetCapacityType capacityType;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeListToMap.class)
    Map<String, Currency> currency;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeListToMap.class)
    Map<String, Pocket> pockets;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeListToMap.class)
    Map<String, Item> items;
}
