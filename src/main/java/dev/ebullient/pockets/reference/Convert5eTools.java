package dev.ebullient.pockets.reference;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import dev.ebullient.pockets.CommonIO;
import dev.ebullient.pockets.Term;
import dev.ebullient.pockets.reference.PocketReference.Compartment;
import picocli.CommandLine.Command;
import picocli.CommandLine.ExitCode;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "5etools", header = "Convert 5etools Json to Pocket Json")
public class Convert5eTools implements Callable<Integer> {

    ObjectMapper mapper = new ObjectMapper();
    Path output;

    @Spec
    CommandSpec spec;

    @Option(names = "-o", description = "Output directory", required = true)
    void setOutputPath(File outputDir) {
        if (outputDir.exists() && outputDir.isFile()) {
            throw new ParameterException(spec.commandLine(),
                    "Specified output path exists and is a file: " + output.toString());
        }
        outputDir.mkdirs();
        output = outputDir.toPath().toAbsolutePath().normalize();
    }

    @Option(names = "-s", description = "Sources")
    List<String> source = Collections.emptyList();

    @Parameters
    List<File> itemFile;

    @Override
    public Integer call() throws Exception {
        mapper.setVisibility(mapper.getVisibilityChecker().withFieldVisibility(Visibility.ANY));
        Index index = new Index();

        for (File f : itemFile) {
            JsonNode node = mapper.readTree(f);
            if (node.has("baseitem")) {
                processItemList(node.get("baseitem"), index);
            }
            if (node.has("item")) {
                processItemList(node.get("item"), index);
            }
        }

        final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        if (!index.isEmpty()) {
            writer.writeValue(output.resolve("index.json").toFile(), index);
            Term.outPrintln("📄 index.json");
        }

        Term.outPrintln("✅ Done.");
        return ExitCode.OK;
    }

    void processItemList(JsonNode itemList, Index index) throws JsonProcessingException, IllegalArgumentException {
        for (Iterator<JsonNode> i = itemList.elements(); i.hasNext();) {
            JsonNode element = i.next();
            final ItemReference item;
            if (element.has("containerCapacity")) {
                PocketReference pocket = new PocketReference();
                pocketReferenceAttributes(element, pocket);
                item = pocket;
            } else {
                item = new ItemReference();
            }
            itemReferenceAttributes(element, item);
            saveValue(element, item, index);
        }
    }

    void saveValue(JsonNode element, ItemReference item, Index index) {
        final Index target = index;

        if (!element.has("srd")) {
            // this isn't an SRD item. Compare it against selected sources
            String itemSource = element.get("source").asText();
            if (itemSource == null || itemSource.isEmpty() || !source.contains(itemSource)) {
                return; // skip this item
            }
        }

        if (item instanceof PocketReference) {
            target.pockets.put(item.idSlug, (PocketReference) item);
        } else {
            target.items.put(item.idSlug, item);
        }
    }

    void pocketReferenceAttributes(JsonNode element, PocketReference pocket)
            throws JsonProcessingException, IllegalArgumentException {
        JsonNode capacity = element.get("containerCapacity");
        pocket.extradimensional = capacity.has("weightless");

        int num = 1;
        double[] weight = null;
        String[] constraints = null;

        if (capacity.has("weight")) {
            weight = mapper.treeToValue(capacity.get("weight"), double[].class);
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
        item.idSlug = CommonIO.slugify(item.name);

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
        }
        Term.outPrintf("Unknown type %s for %s", type, element);
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
