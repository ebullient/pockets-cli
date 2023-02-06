package dev.ebullient.pockets.commands.profile;

import static dev.ebullient.pockets.io.PocketTui.Tui;
import static dev.ebullient.pockets.io.PocketsFormat.BOOKS;
import static dev.ebullient.pockets.io.PocketsFormat.NBSP;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.TypeConversion;
import dev.ebullient.pockets.config.Types.Compartment;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.config.Types.PocketRef;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;
import picocli.CommandLine.Spec;

@Command(name = "presets", header = BOOKS + NBSP + "Generate reference items from 5eTools or Pf2eTools", description = {
        "This will read from a json file containing items or baseitems elements",
        "and will produce a toolsIndex.json file in the specified output directory."
}, footer = {
        "Use the sources option to filter converted items by source. If no sources",
        "are specified, only items from the SRD will be included.",
        "",
        "Specify values as they appear in the exported json, e.g. -s PHB -s DMG.",
        "Only include items from sources you own."
})
public class GeneratePresets implements Callable<Integer> {
    @Spec
    CommandSpec spec;

    @Option(names = "-s", description = "Sources%n  Comma-separated list or multiple declarations")
    List<String> source = Collections.emptyList();

    Path output;

    @Option(names = "-o", description = "Output directory", required = true, scope = ScopeType.INHERIT)
    void setOutputPath(File outputDir) {
        if (outputDir.exists() && outputDir.isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + output.toString());
        }
        outputDir.mkdirs();
        output = outputDir.toPath().toAbsolutePath().normalize();
    }

    @Parameters(paramLabel = "items.json")
    List<File> itemFiles;

    @Override
    public Integer call() throws Exception {
        Tui.debugf("Importing/Converting items from %s to %s. %s",
                itemFiles, output, source.isEmpty() ? "Including only SRD items." : "Including items from " + source);

        if (source.size() == 1 && source.get(0).contains(",")) {
            String tmp = source.remove(0);
            source = List.of(tmp.split(","));
        }
        source = source.stream().map(String::trim).collect(Collectors.toList());
        if (source.stream().anyMatch(x -> x.equals("*") || x.equalsIgnoreCase("all"))) {
            source = List.of("all");
        }

        DataIndex index = new DataIndex();
        for (File f : itemFiles) {
            JsonNode node = Transform.JSON.readTree(f);
            if (node.has("baseitem")) {
                processItemList(node.get("baseitem"), index);
            }
            if (node.has("item")) {
                processItemList(node.get("item"), index);
            }
        }

        if (!index.isEmpty()) {
            final ObjectWriter writer = Transform.JSON.writer(new DefaultPrettyPrinter());
            writer.writeValue(output.resolve("toolsIndex.json").toFile(), index);
            Tui.println("📄 toolsIndex.json");
        }

        Tui.done("Done.");
        return ExitCode.OK;
    }

    void processItemList(JsonNode itemList, DataIndex data) throws Exception {
        for (Iterator<JsonNode> i = itemList.elements(); i.hasNext();) {
            JsonNode element = i.next();
            if (element.has("variants")) {
                // Pf2e variants
                readVariants(element, data);
            } else {
                readItem(element, data);
            }
        }
    }

    void readItem(JsonNode element, DataIndex data) throws JsonProcessingException {
        final ItemRef item;
        if (element.has("containerCapacity")) {
            PocketRef pocket = new PocketRef();
            pocketReferenceAttributes(element, pocket);
            item = pocket;
        } else {
            item = new ItemRef();
        }
        itemReferenceAttributes(element, item);
        saveValue(element, item, data);
    }

    void readVariants(JsonNode element, DataIndex data) throws JsonProcessingException {
        ItemRef baseItem = new ItemRef();
        itemReferenceAttributes(element, baseItem);

        Iterable<JsonNode> it = () -> element.get("variants").elements();
        for (JsonNode variant : it) {
            ItemRef itemVariant = Transform.JSON.treeToValue(Transform.toJson(baseItem), ItemRef.class);
            itemReferenceAttributes(variant, itemVariant);

            String variantType = variant.get("variantType").asText();
            if (Transform.isBlank(itemVariant.name)) {
                if (variantType.equalsIgnoreCase(baseItem.name)) {
                    itemVariant.name = baseItem.name;
                    itemVariant.id = Transform.slugify(baseItem.id);
                } else {
                    variantType = variantType
                            .replace(baseItem.name.toLowerCase(), "")
                            .replaceAll("\\s+", " ")
                            .trim();
                    itemVariant.name = baseItem.name + " (" + variantType + ")";
                    itemVariant.id = Transform.slugify(baseItem.id + " " + variantType);
                }
            }
            if (!itemVariant.name.equals(baseItem.name)) {
                itemVariant.id = Transform.slugify(baseItem.id + " " + variantType);
            }
            saveValue(element, itemVariant, data);
        }
    }

    boolean excludeItem(JsonNode itemSource, boolean isSRD) {
        if (source.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        if (source.contains("all")) {
            return false;
        }
        if (itemSource == null || !itemSource.isTextual()) {
            return true; // unlikely, but skip items if we can't check their source
        }
        return !source.contains(itemSource.asText());
    }

    void saveValue(JsonNode element, ItemRef item, DataIndex data) {
        boolean isSRD = element.has("srd");
        JsonNode itemSource = element.get("source");
        if (excludeItem(itemSource, isSRD)) {
            // skip this item: not from a specified source
            Tui.debugf("Skipped %s from %s (%s)", item.name, itemSource, isSRD);
            return;
        }

        if (item instanceof PocketRef) {
            Tui.debugf("Pocket %s from %s (%s)%n", item.name, itemSource, isSRD);
            data.pocketRef.put(item.id, (PocketRef) item);
        } else {
            Tui.debugf("Item %s from %s (%s)%n", item.name, itemSource, isSRD);
            data.itemRef.put(item.id, item);
        }
    }

    void pocketReferenceAttributes(JsonNode element, PocketRef pocket)
            throws JsonProcessingException, IllegalArgumentException {
        JsonNode capacity = element.get("containerCapacity");
        pocket.magical = capacity.has("weightless");

        int num = 1;
        double[] weight = null;
        String[] constraints = null;

        if (capacity.has("weight")) {
            weight = Transform.JSON.treeToValue(capacity.get("weight"), double[].class);
            num = Math.max(num, weight.length);
        }
        if (capacity.has("item")) {
            JsonNode items = capacity.get("item");
            constraints = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                constraints[i] = items.get(i).toString();
            }
            num = Math.max(num, constraints.length);
        }
        pocket.compartments = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            Compartment c = new Compartment();
            if (weight != null && i < weight.length) {
                c.max_weight = weight[i];
            }
            if (constraints != null && i < constraints.length) {
                c.constraint = constraints[i];
            }
            pocket.compartments.add(c);
        }
    }

    void itemReferenceAttributes(JsonNode element, ItemRef item) throws JsonProcessingException {
        item.name = getName(element);
        item.id = Transform.slugify(item.name);

        // 5e
        if (element.has("weight")) {
            item.weight = element.get("weight").asDouble();
        }
        if (element.has("value")) {
            item.value = element.get("value").asText() + "cp";
        }

        // Pf2e
        if (element.has("bulk")) {
            item.bulk = element.get("bulk").asText();
        }
        JsonNode price = element.get("price");
        if (price != null) {
            String amount = textOrEmpty(price.get("amount"));
            String coin = textOrEmpty(price.get("coin"));
            if ("0".equals(amount) || coin == null) {
                item.value = "0";
            } else {
                item.value = amount + coin;
            }
        }

        String type = textOrEmpty(element.get("type"));
        JsonNode traits = element.get("traits");
        JsonNode category = element.get("category");
        if (traits != null || category != null) {
            // Pf2e has traits
            List<String> t = traits == null ? List.of() : List.of(Transform.JSON.treeToValue(traits, String[].class));
            item.rarity = t.stream()
                    .filter(x -> x.equals("rare") || x.equals("uncommon") || x.equals("magical"))
                    .collect(Collectors.joining(" "));
            if (category == null) {
                item.type = element.has("equipment") ? "equipment" : type;
            } else {
                item.type = Transform.slugify(category.asText());
            }
        } else {
            boolean wondrous = element.has("wondrous") && element.get("wondrous").asBoolean();

            // 5e has types
            if (element.has("type")) {
                item.type = fromCodedType(element);
            } else if (element.has("wondrous")) {
                item.type = "wondrous";
                wondrous = true;
            }

            String rarity = null;
            String tier = null;
            if (element.has("rarity")) {
                String value = element.get("rarity").asText();
                rarity = value.equals("none") ? null : value;
            }
            if (element.has("tier")) {
                tier = element.get("tier").asText();
            }

            if (rarity != null || tier != null) {
                item.rarity = (tier == null ? "" : tier)
                        + (tier != null && rarity != null ? " " : "")
                        + (rarity == null ? "" : rarity);
            }
            if (item.magical == null) {
                item.magical = wondrous; // perhaps set by pocket attributes
            }
        }
    }

    private String fromCodedType(JsonNode element) {
        String type = element.get("type").asText();
        switch (type) {
            case "LA":
                return "light-armor";
            case "MA":
                return "medium-armor";
            case "HA":
                return "heavy-armor";
            case "S":
                return "shield";

            case "M":
                return "melee-weapon";
            case "R":
                return "ranged-weapon";
            case "A":
                return "ammunition";
            case "AF":
                return "ammunition-firearm";
            case "EXP":
                return "explosive";

            case "RD":
                return "rod";
            case "ST":
                return "staff";
            case "WD":
                return "wand";
            case "RG":
                return "ring";
            case "P":
                return "potion";
            case "SC":
                return "scroll";

            case "W":
                return "wondrous";
            case "$":
                return "coins-gems";

            case "G":
                return "adventuring-gear";

            case "SCF":
                return "spellcaster-focus";

            case "INS":
                return "instrument";
            case "AT":
                return "artisans-tools";

            case "OTH":
            case "T":
                return "tools";
            case "GS":
                return "gaming-set";

            case "FD":
                return "food";
            case "TG":
                return "trade-good";

            case "MNT":
                return "mounts";
            case "TAH":
                return "tack-harness";

            case "AIR":
            case "SHP":
            case "VEH":
            case "SPC":
                return "vehicle";

            case "MR":
                return "master-rune";
        }
        Tui.debugf("Unknown type %s for %s", type, element);
        return null;
    }

    String textOrEmpty(JsonNode textNode) {
        return textNode == null ? "" : textNode.asText();
    }

    /**
     * From 5eTools, if the item is in the SRD, the SRD name is in the
     * "srd" field (rather than just true/false).
     *
     * @param element
     * @return String to use as the name
     */
    String getName(JsonNode element) {
        JsonNode srd = element.get("srd");
        if (srd != null) {
            if (srd.isTextual()) {
                return srd.asText();
            }
        }
        return textOrEmpty(element.get("name"));
    }

    static class DataIndex {
        @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
        @JsonDeserialize(using = TypeConversion.DeserializePocketListToMap.class)
        Map<String, PocketRef> pocketRef = new TreeMap<>();

        @JsonSerialize(using = TypeConversion.SerializeMapToList.class)
        @JsonDeserialize(using = TypeConversion.DeserializeItemListToMap.class)
        Map<String, ItemRef> itemRef = new TreeMap<>();

        @JsonIgnore
        public boolean isEmpty() {
            return itemRef.isEmpty() && pocketRef.isEmpty();
        }
    }
}
