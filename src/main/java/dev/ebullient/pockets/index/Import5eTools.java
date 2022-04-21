package dev.ebullient.pockets.index;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectWriter;

import dev.ebullient.pockets.PocketsImport;
import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.index.Index.IndexData;
import dev.ebullient.pockets.index.PocketReference.Compartment;
import dev.ebullient.pockets.io.PocketTui;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;
import picocli.CommandLine.Spec;

@Command(name = "5etools", header = "Create Pockets reference items from 5eTools items", description = {
        "This will read from a 5etools json file containing items or baseitems elements",
        "and will produce a 5toolsIndex.json file in the specified output directory."
}, footer = {
        "Use the sources option to filter converted items by source. If no sources",
        "are specified, only items from the SRD will be included.",
        "Specify values as they appear in the exported json, e.g. -s PHB -s DMG.",
        "Only include items from sources you own."
})
public class Import5eTools implements Callable<Integer> {
    @Spec
    CommandSpec spec;

    @Inject
    PocketTui tui;

    @ParentCommand
    PocketsImport parent;

    @Option(names = "-s", description = "Sources%n  Comma-separated list or multiple declarations")
    List<String> source = Collections.emptyList();

    @Parameters(paramLabel = "5etools.json")
    List<File> itemFile;

    @Override
    public Integer call() throws Exception {
        Path output = parent.getOutputPath();
        tui.debugf("Importing/Converting items from 5e tools %s to %s. %s",
                itemFile, output, source.isEmpty() ? "Including only SRD items." : "Including items from " + source);

        IndexData data = new IndexData();
        for (File f : itemFile) {
            JsonNode node = IndexConstants.FROM_JSON.readTree(f);
            if (node.has("baseitem")) {
                processItemList(node.get("baseitem"), data);
            }
            if (node.has("item")) {
                processItemList(node.get("item"), data);
            }
        }

        if (!data.isEmpty()) {
            final ObjectWriter writer = IndexConstants.FROM_JSON.writer(new DefaultPrettyPrinter());
            writer.writeValue(output.resolve("5etoolsIndex.json").toFile(), data);
            tui.outPrintln("📄 5etoolsIndex.json");
        }

        tui.done("Done.");
        return ExitCode.OK;
    }

    void processItemList(JsonNode itemList, IndexData index) throws JsonProcessingException, IllegalArgumentException {
        for (Iterator<JsonNode> i = itemList.elements(); i.hasNext();) {
            JsonNode element = i.next();
            final ItemReference item;
            if (element.has("containerCapacity")) {
                PocketReference pocket = new PocketReference();
                pocketReferenceAttributes(element, pocket);
                item = (ItemReference) pocket;
            } else {
                item = new ItemReference();
            }
            itemReferenceAttributes(element, item);
            saveValue(element, item, index);
        }
    }

    void saveValue(JsonNode element, ItemReference item, IndexData index) {
        final IndexData target = index;

        boolean isSRD = element.has("srd");
        JsonNode itemSource = element.get("source");
        if (excludeItem(itemSource, isSRD)) {
            // skip this item: not from a specified source
            tui.debugf("Skipped %s from %s (%s)", item.name, itemSource, isSRD);
            return;
        }

        if (item instanceof PocketReference) {
            tui.verbosef("Pocket %s from %s (%s)%n", item.name, itemSource, isSRD);
            target.pockets.put(item.idSlug, (PocketReference) item);
        } else {
            tui.verbosef("Item %s from %s (%s)%n", item.name, itemSource, isSRD);
            target.items.put(item.idSlug, item);
        }
    }

    boolean excludeItem(JsonNode itemSource, boolean isSRD) {
        if (source.isEmpty()) {
            return !isSRD; // exclude non-SRD sources when no filter is specified.
        }
        if (itemSource == null || !itemSource.isTextual()) {
            return true; // unlikely, but skip items if we can't check their source
        }
        return !source.contains(itemSource.asText());
    }

    void pocketReferenceAttributes(JsonNode element, PocketReference pocket)
            throws JsonProcessingException, IllegalArgumentException {
        JsonNode capacity = element.get("containerCapacity");
        pocket.extradimensional = capacity.has("weightless");

        int num = 1;
        double[] weight = null;
        String[] constraints = null;

        if (capacity.has("weight")) {
            weight = IndexConstants.FROM_JSON.treeToValue(capacity.get("weight"), double[].class);
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

    void itemReferenceAttributes(JsonNode element, ItemReference item) {
        item.name = getName(element);
        item.idSlug = Mapper.slugify(item.name);

        if (element.has("weight")) {
            item.weight = element.get("weight").asDouble();
        }
        if (element.has("value")) {
            item.value = element.get("value").toString() + "cp";
        }

        if (element.has("wondrous")) {
            item.wondrous = true;
        }
        if (element.has("rarity")) {
            item.rarity = element.get("rarity").asText();
        }
        if (element.has("tier")) {
            item.tier = element.get("tier").asText();
        }
        if (element.has("type")) {
            item.type = fromCodedType(element);
        } else if (item.wondrous) {
            item.type = "wondrous";
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
                return "vehicle";

            case "MR":
                return "master-rune";
        }
        tui.debugf("Unknown type %s for %s", type, element);
        return null;
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
        return element.get("name").asText();
    }
}
