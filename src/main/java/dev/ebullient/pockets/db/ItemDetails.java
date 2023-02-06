package dev.ebullient.pockets.db;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;

public class ItemDetails {
    public String refId; // optional (input parameter)
    public String name; // optional (input parameter)

    public String notes;

    public String bulk;
    public Double weight;
    public Double baseUnitValue;

    public Boolean tradable;
    public Boolean fullValueTrade;
    public Boolean magical;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ItemDetails that = (ItemDetails) o;
        return tradable == that.tradable
                && Objects.equals(refId, that.refId)
                && Objects.equals(name, that.name)
                && Objects.equals(notes, that.notes)
                && Objects.equals(bulk, that.bulk)
                && Objects.equals(weight, that.weight)
                && Objects.equals(magical, that.magical)
                && Objects.equals(fullValueTrade, that.fullValueTrade)
                && Objects.equals(baseUnitValue, that.baseUnitValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(refId, name, notes, bulk, weight, baseUnitValue, tradable, fullValueTrade, magical);
    }

    public ItemDetails setRefId(String refId) {
        if (this.refId == null) {
            this.refId = refId;
        }
        return this;
    }

    public ItemDetails setName(String name) {
        this.name = name;
        return this;
    }

    public ItemDetails setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public ItemDetails setBulk(String bulk) {
        this.bulk = bulk;
        return this;
    }

    public ItemDetails setWeight(Double weight) {
        this.weight = weight;
        return this;
    }

    public ItemDetails setBaseUnitValue(Double baseUnitValue) {
        this.baseUnitValue = baseUnitValue;
        return this;
    }

    public ItemDetails setTradable(Boolean tradable) {
        this.tradable = tradable;
        return this;
    }

    public ItemDetails setMagical(Boolean magical) {
        this.magical = magical;
        return this;
    }

    public String asMemo(ProfileConfigData cfg) {
        List<String> bits = new ArrayList<>();
        if (refId != null) {
            bits.add("based on '" + refId + "'");
        }
        bits.add(cfg.bulkToString(bulk));
        bits.add(cfg.weightToString(weight));
        bits.add(cfg.valueToMemo(baseUnitValue));
        if (Transform.isTrue(tradable)) {
            bits.add("tradable");
        }
        return bits.stream().filter(x -> !Transform.isBlank(x)).collect(Collectors.joining("; "));
    }

    public String bulkWeight(ProfileConfigData cfg) {
        if (cfg.preset == PresetFlavor.pf2e) {
            return cfg.bulkToString(bulk);
        }
        return cfg.weightToString(weight);
    }
}
