package dev.ebullient.pockets.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfigData;

public class PocketDetails extends ItemDetails {

    public String emoji; // optional: web parameter (sometimes present)

    @JsonProperty("max_weight")
    public Double maxWeight;
    @JsonProperty("max_volume")
    public Double maxVolume;
    @JsonProperty("max_bulk")
    public String maxBulk;

    public Boolean bottomless;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PocketDetails))
            return false;
        if (!super.equals(o))
            return false;
        PocketDetails that = (PocketDetails) o;
        return Objects.equals(emoji, that.emoji)
                && Objects.equals(maxWeight, that.maxWeight)
                && Objects.equals(maxVolume, that.maxVolume)
                && Objects.equals(maxBulk, that.maxBulk)
                && Objects.equals(bottomless, that.bottomless);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), emoji, maxWeight, maxVolume, maxBulk, bottomless);
    }

    public PocketDetails setRefId(String refId) {
        super.setRefId(refId);
        return this;
    }

    public PocketDetails setName(String name) {
        super.setName(name);
        return this;
    }

    public PocketDetails setEmoji(String emoji) {
        this.emoji = emoji;
        return this;
    }

    public PocketDetails setMaxWeight(Double max_weight) {
        this.maxWeight = max_weight;
        return this;
    }

    public PocketDetails setMaxVolume(Double max_volume) {
        this.maxVolume = max_volume;
        return this;
    }

    public PocketDetails setMaxBulk(String max_bulk) {
        this.maxBulk = max_bulk;
        return this;
    }

    public PocketDetails setBottomless(Boolean bottomless) {
        this.bottomless = bottomless;
        return this;
    }

    public String asMemo(ProfileConfigData cfg) {
        List<String> bits = new ArrayList<>();
        bits.add(super.asMemo(cfg));
        String weightStr = cfg.weightToString(maxWeight);
        String volStr = cfg.volumeToString(maxVolume);
        if (!weightStr.isEmpty() || !volStr.isEmpty()) {
            String weightAndVol = !weightStr.isEmpty() && !volStr.isEmpty() ? " or " : "";
            bits.add("can hold at most " + weightStr + weightAndVol + volStr);
        }
        if (maxBulk != null) {
            bits.add("can hold at most " + cfg.bulkToString(maxBulk));
        }
        if (Transform.isTrue(magical)) {
            bits.add("extradimensional");
        }
        return bits.stream().filter(x -> !Transform.isBlank(x)).collect(Collectors.joining("; "));
    }
}
