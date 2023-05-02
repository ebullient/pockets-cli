package dev.ebullient.pockets.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.PocketDetails;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.io.InvalidPocketState;

public interface Types {

    enum ProfileConfigDataState {
        isNew,
        hasData,
        empty
    }

    enum PresetFlavor {
        dnd5e,
        pf2e
    }

    enum PresetCapacityType {
        bulk,
        weight
    }

    class PocketConfigData {
        public String activeProfile;
        public Map<String, ProfileConfigData> profiles = new HashMap<>();

        @Override
        public String toString() {
            return "PocketConfigData [activeProfile=" + activeProfile + ", profiles=" + profiles + "]";
        }
    }

    static <T extends ItemRef> Map<String, T> merge(ProfileConfigData newCfg, Map<String, T> orig, Map<String, T> updates) {
        if (updates == null) {
            return orig;
        }
        if (orig == null) {
            return updates;
        }

        // Do not delete old reference types, they could be referenced
        updates.entrySet().stream() // add new
                .filter(e -> !orig.containsKey(e.getKey()))
                .forEach(e -> orig.put(e.getKey(), e.getValue()));

        updates.entrySet().stream() // update those present in both
                .filter(e -> orig.containsKey(e.getKey()))
                .forEach(e -> orig.get(e.getKey()).merge(newCfg, e.getValue()));

        return orig;
    }

    static <D extends ItemDetails> void applyGenericDefaults(ItemType type, D details) {
        details.tradable = details.tradable == null ? details.baseUnitValue != null : details.tradable;
        if (details.fullValueTrade == null) {
            details.fullValueTrade = Transform.isFalse(details.magical) && details.tradable;
        }
        if (type == ItemType.POCKET) {
            PocketDetails pd = ((PocketDetails) details);
            pd.emoji = pd.emoji == null ? "🥡" : pd.emoji;
        }
    }

    @JsonInclude(Include.NON_DEFAULT)
    class ItemRef {
        public String id;
        public String name;

        public String value; // price w/ unit 6cp, 5gp
        // used for items cheaper than one unit, e.g. Covers 5 sacks for 1cp
        public Integer quantity = 1;

        public Double weight; // weight of the pocket itself in lbs
        public String bulk; // negligible (-), light (L), 1, 2, 3, 4, 5

        public Double unitValue;
        public String type;
        public String rarity;
        public Boolean magical;

        public void initialize(ProfileConfigData profileConfigData) {
            if (value != null) {
                unitValue = profileConfigData.toBaseValue(value);
            }
        }

        public void applyDefaults(ItemDetails details) {
            details.name = details.name == null ? name : details.name;
            details.bulk = details.bulk == null ? bulk : details.bulk;
            details.weight = details.weight == null ? weight : details.weight;
            details.baseUnitValue = details.baseUnitValue == null ? unitValue : details.baseUnitValue;
            details.magical = details.magical == null ? magical : details.magical;

            boolean hasValue = this.value != null || details.baseUnitValue != null;
            details.tradable = details.tradable == null ? hasValue : details.tradable;
            if (details.fullValueTrade == null) {
                if (type != null && type.contains("coins")) {
                    details.fullValueTrade = true;
                } else if (type == null) {
                    details.fullValueTrade = Transform.isTrue(details.tradable) && Transform.isFalse(details.magical);
                }
            }
        }

        public void merge(ProfileConfigData newCfg, ItemRef updated) {
            // update/replace those attributes that are safe to update (not id)
            name = updated.name == null ? name : updated.name;
            bulk = updated.bulk == null ? bulk : updated.bulk;
            weight = updated.weight == null ? weight : updated.weight;
            value = updated.value == null ? value : updated.value;
            quantity = updated.quantity == null ? quantity : updated.quantity;
            if (unitValue != null) {
                unitValue = null; // clear previous
                initialize(newCfg); // recompute
            }
        }

        @Override
        public String toString() {
            return "ItemRef{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", value='" + value + '\'' +
                    ", quantity=" + quantity +
                    ", weight=" + weight +
                    ", bulk='" + bulk + '\'' +
                    ", unitValue=" + unitValue +
                    ", magical=" + magical +
                    '}';
        }
    }

    class CurrencyRef extends ItemRef {
        public String notation;
        public double unitConversion;

        @Override
        public String toString() {
            return "CurrencyRef{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", notation='" + notation + '\'' +
                    ", unitConversion=" + unitConversion +
                    '}';
        }
    }

    class PocketRef extends ItemRef {
        public String emoji;
        public Boolean bottomless;
        public List<Compartment> compartments;
        public Double maxWeight;
        public Double maxVolume;
        public String maxBulk;

        public void initialize(ProfileConfigData profileConfigData) {
            super.initialize(profileConfigData);
            switch (profileConfigData.preset) {
                case dnd5e:
                    if (maxWeight == null) {
                        calculateMaxWeight();
                    }
                    if (maxVolume == null) {
                        calculateMaxVolume();
                    }
                    break;
                case pf2e:
                    if (maxBulk == null) {
                        calculateMaxBulk();
                    }
                    break;
            }
        }

        private void calculateMaxWeight() {
            try {
                maxWeight = compartments.stream()
                        .filter(x -> x.max_weight != null)
                        .mapToDouble(x -> x.max_weight)
                        .sum();
            } catch (NumberFormatException nfe) {
                throw new InvalidPocketState("Pocket reference contains invalid max weight value: %s",
                        Transform.toJsonString(compartments));
            }
        }

        private void calculateMaxVolume() {
            try {
                maxVolume = compartments.stream()
                        .filter(x -> x.max_volume != null)
                        .mapToDouble(x -> x.max_volume)
                        .sum();
            } catch (NumberFormatException nfe) {
                throw new InvalidPocketState("Pocket reference contains invalid max Bulk value: %s",
                        Transform.toJsonString(compartments));
            }
        }

        private void calculateMaxBulk() {
            try {
                maxBulk = compartments.stream()
                        .filter(x -> x.max_bulk != null)
                        .mapToInt(x -> Integer.parseInt(x.max_bulk))
                        .sum() + "";
            } catch (NumberFormatException nfe) {
                throw new InvalidPocketState("Pocket reference contains invalid max Bulk value: %s",
                        Transform.toJsonString(compartments));
            }
        }

        public void applyDefaults(ItemDetails details) {
            super.applyDefaults(details);
            PocketDetails pd = (PocketDetails) details;
            pd.emoji = pd.emoji == null ? emoji : pd.emoji;
            pd.fullValueTrade = false; // pockets are always either half-value (equipment) or magical
            pd.bottomless = pd.bottomless == null ? bottomless : pd.bottomless;
            pd.maxWeight = pd.maxWeight == null ? maxWeight : pd.maxWeight;
            pd.maxVolume = pd.maxVolume == null ? maxVolume : pd.maxVolume;
            pd.maxBulk = pd.maxBulk == null ? maxBulk : pd.maxBulk;
            pd.notes = pd.notes == null ? compartments.stream()
                    .filter(x -> x.constraint != null)
                    .map(x -> x.constraint)
                    .collect(Collectors.joining(" ")) : pd.notes;
        }

        public void merge(ProfileConfigData newCfg, PocketRef value) {
            // update/replace those attributes that are safe to update (not id)
            super.merge(newCfg, value);
            emoji = value.emoji == null ? emoji : value.emoji;
            compartments = value.compartments == null ? compartments : value.compartments;
            if (value.compartments != null) {
                calculateMaxWeight();
                calculateMaxVolume();
                calculateMaxBulk();
            }
        }

        @Override
        public String toString() {
            return "PocketRef{" +
                    ", id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", emoji='" + emoji + '\'' +
                    ", compartments=" + compartments +
                    ", max_weight=" + maxWeight +
                    ", max_volume=" + maxVolume +
                    ", max_bulk='" + maxBulk + '\'' +
                    ", value='" + value + '\'' +
                    ", quantity=" + quantity +
                    ", weight=" + weight +
                    ", bulk='" + bulk + '\'' +
                    ", unitValue=" + unitValue +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class Compartment {
        public String constraint;
        // For Pathfinder (Bulk) encumbrance rules
        public String max_bulk;

        // For D&D 5e (weight-based) encumbrance rules
        public Double max_weight; // in lbs
        public Double max_volume; // in cubic feet
    }
}
