package dev.ebullient.pockets.config;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.Types.CurrencyRef;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.config.Types.PocketRef;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.config.Types.ProfileConfigDataState;
import dev.ebullient.pockets.io.InvalidPocketState;
import picocli.CommandLine.ExitCode;

public class ProfileConfigData {
    public static final Pattern COIN_QUANTITY = Pattern.compile("([\\d*.]+)(.+)");

    public static ProfileConfigData create(String name) {
        ProfileConfigData pcd = new ProfileConfigData();
        pcd.slug = Transform.slugify(name);
        if (!"default".equals(name)) {
            pcd.description = name;
        }
        return pcd;
    }

    @JsonIgnore
    private boolean initialized;

    @JsonIgnore
    private PresetValues presetValues;

    @JsonProperty(value = "id")
    public String slug;

    public String description;
    public PresetFlavor preset;
    public ProfileConfigDataState state;

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeCurrencyListToMap.class)
    Map<String, CurrencyRef> currency = new HashMap<>();

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializePocketListToMap.class)
    Map<String, PocketRef> pocketRef = new HashMap<>();

    @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
    @JsonDeserialize(using = TypeConversion.DeserializeItemListToMap.class)
    Map<String, ItemRef> itemRef = new HashMap<>();

    public void initialize() {
        if (preset == null) {
            Tui.warnf("Making a modification to a profile that does not have a preset defined. Using default (%s).",
                    PresetFlavor.dnd5e);
            preset = PresetFlavor.dnd5e;
        }
        validate();
    }

    public CurrencyRef currencyRef(String refId) {
        initialize();
        CurrencyRef ref = currency.getOrDefault(refId, presetValues.currency.get(refId));
        if (ref == null) {
            throw new InvalidPocketState(true, "Unknown currency reference: %s", refId);
        }
        ref.initialize(this);
        return ref;
    }

    public ItemRef itemRef(String refId) {
        initialize();
        ItemRef ref = itemRef.getOrDefault(refId, presetValues.itemRef.get(refId));
        if (ref == null) {
            return null;
        }
        ref.initialize(this);
        return ref;
    }

    public PocketRef pocketRef(String refId) {
        initialize();
        PocketRef ref = pocketRef.getOrDefault(refId, presetValues.pocketRef.get(refId));
        if (ref == null) {
            throw new InvalidPocketState(true, "Unknown pocket reference (%s) in preset (%s)", refId, preset);
        }
        ref.initialize(this);
        return ref;
    }

    public boolean hasPocketRef(String refId) {
        return pocketRef.containsKey(refId) || presetValues.pocketRef.containsKey(refId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProfileConfigData))
            return false;
        ProfileConfigData that = (ProfileConfigData) o;
        return Objects.equals(slug, that.slug)
                && Objects.equals(description, that.description)
                && preset == that.preset && state == that.state
                && Objects.equals(currency, that.currency)
                && Objects.equals(pocketRef, that.pocketRef)
                && Objects.equals(itemRef, that.itemRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug, description, preset, state, currency, pocketRef, itemRef);
    }

    @Override
    public String toString() {
        return "ProfileConfig[slug=" + slug
                + ", preset=" + preset
                + ", currency=" + Transform.sizeOrNull(currency)
                + ", pocketRef=" + Transform.sizeOrNull(pocketRef)
                + ", itemRef=" + Transform.sizeOrNull(itemRef)
                + ", state=" + state
                + "]";
    }

    public void validate() {
        if (!initialized) {
            List<String> errors = new ArrayList<>();
            if (!validate(errors)) {
                throw new InvalidPocketState(ExitCode.USAGE,
                        "Profile reference types (preset or custom) contain validation errors:%n%s",
                        String.join("\n  ", errors));
            }
            initialized = true;
        }
    }

    public boolean validate(List<String> errors) {
        if (preset != null && presetValues == null) {
            presetValues = Presets.getPresets(preset);
            validate(errors, presetValues.currency);
            validate(errors, presetValues.pocketRef);
            validate(errors, presetValues.itemRef);
        }
        validate(errors, currency);
        validate(errors, pocketRef);
        validate(errors, itemRef);
        return errors.isEmpty();
    }

    private <T extends ItemRef> void validate(List<String> errors, Map<String, T> map) {
        if (map == null) {
            return;
        }
        map.forEach((k, v) -> {
            if (!k.equals(v.id)) {
                errors.add(String.format("Mismatch between key %s and id value %s", k, v));
            }
        });
    }

    public boolean invalidPresetModification(ProfileConfigData newCfg) {
        // the change is invalid if the preset is set in both,
        // and the values don't match. An empty preset value in the inbound
        // configuration is the result of a profile reset (data)
        return !Transform.isEmpty(preset) && !Transform.isEmpty(newCfg.preset)
                && preset != newCfg.preset;
    }

    public void merge(ProfileConfigData newCfg) {
        if (invalidPresetModification(newCfg)) {
            throw new InvalidPocketState(ExitCode.USAGE,
                    "Unable to import settings. You can not change the preset associated with a profile once it has been set.");
        }
        description = newCfg.description;
        preset = newCfg.preset;

        // Not everything can be changed once referenced..
        currency = Types.merge(newCfg, currency, newCfg.currency);
        pocketRef = Types.merge(newCfg, pocketRef, newCfg.pocketRef);
        itemRef = Types.merge(newCfg, itemRef, newCfg.itemRef);

        validate();
    }

    public String bulkToString(String bulk) {
        if (Transform.isBlank(bulk)) {
            return "";
        }
        switch (bulk.toLowerCase()) {
            case "-":
            case "negligible":
                return "negligible";
            case "l":
            case "light":
                return "light";
            default:
                return bulk + " Bulk";
        }
    }

    public String weightToString(Double weight) {
        if (weight == null || weight == 0) {
            return "";
        }
        if (weight == 1) {
            return "1 pound";
        }
        return weight + " pounds";
    }

    public String volumeToString(Double volume) {
        if (volume == null || volume == 0) {
            return "";
        }
        if (volume == 1) {
            return "1 cubic foot";
        }
        return volume + " cubic feet";
    }

    public String valueToMemo(Double value) {
        if (value == null || value == 0) {
            return "";
        }
        if (preset == PresetFlavor.dnd5e || preset == PresetFlavor.pf2e) {
            FantasyCoins coins = new FantasyCoins(value);
            return "worth " + coins;
        }
        return "worth " + value;
    }

    public String valueToString(Double value) {
        if (value == null || value == 0) {
            return "";
        }
        if (preset == PresetFlavor.dnd5e || preset == PresetFlavor.pf2e) {
            FantasyCoins coins = new FantasyCoins(value);
            return coins.toString();
        }
        return Double.toString(value);
    }

    public Double toBaseValue(String value) {
        Matcher m = COIN_QUANTITY.matcher(value);
        if (m.matches()) {
            CurrencyRef cRef = currencyRef(m.group(2));
            int quantity = Integer.parseInt(m.group(1)); // amount
            if (quantity <= 0) {
                throw new InvalidPocketState(true, "Do not use negative currency values: %s", quantity);
            }
            return quantity * cRef.unitConversion;
        } else {
            throw new InvalidPocketState(true, "Invalid currency value: %s", value);
        }
    }

    public Map<String, Long> parseCurrencyChanges(String value) {
        Map<String, Long> quantities = new HashMap<>();
        Set<String> fullAmount = new HashSet<>();
        for (String s : value.trim().split(" ")) {
            Matcher m = COIN_QUANTITY.matcher(s);
            if (m.matches()) {
                String cid = m.group(2);
                if ("*".equals(m.group(1))) {
                    fullAmount.add(cid);
                } else {
                    Long quantity = Long.parseLong(m.group(1)); // amount
                    if (quantity < 0) {
                        throw new InvalidPocketState(true, "Invalid amount %s (value should always be positive)", s);
                    } else if (quantity == 0) {
                        continue; // no change
                    }
                    quantities.merge(cid, quantity, Long::sum);
                }
            } else {
                throw new InvalidPocketState(true, "Unknown currency value %s", value);
            }
        }
        fullAmount.forEach(x -> quantities.put(x, null)); // null values for '*' (all)
        return quantities;
    }

    public boolean isCurrencyString(String value) {
        if (Transform.isBlank(value)) {
            return false;
        }
        String[] bits = value.trim().split(" ");
        return Arrays.stream(bits).allMatch(s -> COIN_QUANTITY.matcher(s).matches());
    }

    static class FantasyCoins {
        int pp;
        int gp;
        int sp;
        int cp;

        FantasyCoins(double value) {
            int remainder = (int) value;
            pp = remainder / 1000;
            remainder = remainder % 1000;
            gp = remainder / 100;
            remainder = remainder % 100;
            sp = remainder / 10;
            remainder = remainder % 10;
            cp = remainder;
        }

        @Override
        public String toString() {
            List<String> result = new ArrayList<>();
            if (pp > 0) {
                result.add(pp + "pp");
            }
            if (gp > 0) {
                result.add(gp + "gp");
            }
            if (sp > 0) {
                result.add(sp + "sp");
            }
            if (cp > 0) {
                result.add(cp + "cp");
            }
            return String.join(", ", result);
        }
    }
}
