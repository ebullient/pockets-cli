package dev.ebullient.pockets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;

import com.github.slugify.Slugify;

import dev.ebullient.pockets.Config.Cache;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import dev.ebullient.pockets.reference.PocketReference;

/**
 * Common output formatting and data manipulation for either parameters
 * or return types. Common across many commands.
 */
@ApplicationScoped
public class CommonIO {

    private static Slugify slugify;
    final Config config;
    final Cache cache;

    public CommonIO(Config config) {
        this.config = config;
        this.cache = config.getCache();
    }

    public void checkFieldWidths(Pocket pocket) {
        cache.checkFieldWidths();
    }

    public void checkFieldWidths(PocketItem item) {
        cache.checkFieldWidths();
    }

    public Pocket selectPocketById(Long pocketId) {
        Pocket pocket = Pocket.findById(pocketId);
        if (pocket == null) {
            Term.outPrintf("%nThe specified value [%s] doesn't match any of your pockets.%n", pocketId);
            listAllPockets();
            return null;
        }
        return pocket;
    }

    public PocketItem selectPocketItemById(Pocket pocket, Long item_id) {
        PocketItem item = PocketItem.findById(item_id);
        if (item == null || !item.belongsTo(pocket)) {
            Term.outPrintf("%nThe specified value [%s] doesn't match any of the items in this pocket.%n", item_id);
            return null;
        }
        return item;
    }

    public Pocket selectPocketByName(String name) {
        List<Pocket> pockets = Pocket.findByName(name);

        if (pockets.size() > 1) {
            Term.outPrintf("%nSeveral pockets match '%s':%n", name);
            tableOfPockets(pockets);
            Term.outPrintln("");

            if (Term.canPrompt()) {
                String line = Term.prompt("Which pocket did you mean [ID, or empty to cancel]? ");
                Optional<Long> newId = toLong(line, false);
                if (newId.isPresent()) {
                    return selectPocketById(newId.get());
                }
            }
            Term.outPrintln("Unable to choose a pocket. Please be more specific.");
        } else if (pockets.size() == 1) {
            return pockets.iterator().next();
        } else {
            Term.outPrintf("%n'%s' doesn't match any of your pockets.%n", name);
            listAllPockets();
        }
        return null;
    }

    public PocketItem selectPocketItemByName(Pocket pocket, String name) {
        List<PocketItem> items = PocketItem.findByName(pocket, name);

        if (items.size() > 1) {
            Term.outPrintf("%nSeveral items match '%s':%n", name);
            tableOfPocketItems(items);
            Term.outPrintln("");

            if (Term.canPrompt()) {
                String line = Term.prompt("Which item did you mean [ID, or empty to cancel]? ");
                Optional<Long> newId = toLong(line, false);
                if (newId.isPresent()) {
                    return selectPocketItemById(pocket, newId.get());
                }
            }
            Term.outPrintln("Unable to choose an item. Please be more specific.");
        } else if (items.size() == 1) {
            return items.iterator().next();
        } else {
            Term.outPrintf("%n'%s' doesn't match any of the items in your pocket.%n", name);
            listPocketContents(pocket);
        }
        return null;
    }

    void listAllPockets() {
        Term.outPrintln("\n 🛍 Your pockets:\n");
        tableOfPockets(Pocket.listAll());
        Term.outPrintln("");
    }

    void listPocketContents(Pocket pocket) {
        Term.debug(pocket.toString());
        if (pocket.items.isEmpty()) {
            Term.outPrintf("%n%-2s %s [%d] is empty.%n%n", getPocketEmoji(pocket), pocket.name, pocket.id);
            describe(pocket);
        } else {
            Term.outPrintf("%n%-2s %s [%d] contains:%n%n", getPocketEmoji(pocket), pocket.name, pocket.id);
            tableOfPocketItems(pocket.items);
            Term.outPrintln("");
            describe(pocket);
        }
    }

    private void tableOfPockets(List<Pocket> pockets) {
        Term.outPrintln(cache.ansiFormat("p.th"));
        Term.outPrintln(cache.ansiFormat("p.thr"));
        pockets.forEach(
                p -> Term.outPrintf(cache.ansiFormat("p.tr"), p.id, getPocketEmoji(p),
                        p.extradimensional ? "*" : " ",
                        p.name));
    }

    private void tableOfPocketItems(Collection<PocketItem> items) {
        Term.outPrintln(cache.ansiFormat("pi.th"));
        Term.outPrintln(cache.ansiFormat("pi.thr"));
        items.forEach(
                i -> Term.outPrintf(cache.ansiFormat("pi.tr"), i.id, i.quantity, i.name,
                        i.weight == null ? "-" : i.weight,
                        i.gpValue == null ? "-" : i.gpValue));
    }

    public void describe(Pocket pocket) {
        describe(pocket, getPocketReference(pocket.pocketRef));
    }

    public void describe(Pocket pocket, PocketReference pRef) {
        if (pocket.extradimensional) {
            Term.outPrintf(
                    "@|bold,underline This %s is magical.|@%nIt always weighs %s, regardless of its contents.%n",
                    pRef.name, weightUnits(pocket.weight));
        } else {
            Term.outPrintf("This %s weighs %s when empty.%n", pRef.name, weightUnits(pocket.weight));
        }

        String weight = "";
        if (pocket.max_weight != null && pocket.max_weight != 0) {
            weight = weightUnits(pocket.max_weight);
        }
        String volume = "";
        if (pocket.max_volume != null && pocket.max_volume != 0) {
            volume = volumeUnits(pocket.max_volume);
        }

        Term.outPrintf("It can hold %s%s%s of gear.%n",
                weight,
                (weight.length() > 0 && volume.length() > 0) ? " or " : "",
                volume);

        if (pRef.hasConstraints()) {
            Term.outPrintln(pRef.constraint(pocket.slug));
        }
    }

    public void describe(PocketItem item) {
        Term.outPrintln("  Quantity     : " + item.quantity);
        Term.outPrintln("  Weight (lbs) : " + (item.weight == null ? "unknown" : item.weight));
        Term.outPrintln("  Value (gp)   : " + (item.gpValue == null ? "unknown" : item.gpValue));
    }

    public void dumpStatistics() {
        long pockets = Pocket.count();
        long pocketitem = PocketItem.count();
        Term.outPrintf("%nYou have %s containing %s.%n",
                howMany(pockets, "pocket", "pockets"),
                howMany(pocketitem, "pocket item", "pocket items"));
    }

    public String getPocketEmoji(Pocket pocket) {
        PocketReference ref = config.getPocketReference(pocket);
        String emoji = ref.getEmoji();
        if (emoji == null) {
            emoji = ref.setEmoji(cache.getPocketEmoji(ref.idSlug));
        }
        return emoji;
    }

    public PocketReference getPocketReference(String pocketRef) {
        return config.getPocketReference(pocketRef);
    }

    public static void pocketTableFormat(Map<String, Integer> fieldWidths, Map<String, String> ansiFormat) {
        int idWidth = fieldWidths.get("p.id");
        ansiFormat.put("p.th",
                String.format("@|faint [%" + idWidth + "s]     %-50s |@", "ID ", "Name"));

        ansiFormat.put("p.thr",
                String.format("@|faint -%s-+--+-%s-|@", "-".repeat(idWidth), "-".repeat(50)));

        ansiFormat.put("p.tr", "@|faint [|@%" + idWidth + "d@|faint ]|@ %-2s %s%-50s%n");
    }

    public static void pocketItemTableFormat(Map<String, Integer> fieldWidths, Map<String, String> ansiFormat) {
        int id = fieldWidths.get("pi.id");
        int q = fieldWidths.get("pi.quantity");
        int w = fieldWidths.get("pi.weight");
        int gp = fieldWidths.get("pi.gpValue");

        ansiFormat.put("pi.th",
                String.format("@|faint [%" + id + "s] (%" + q + "s)  %-50s   %" + w + "s   %" + gp + "s|@",
                        "ID ", "Q ", "Name / Description", "lbs", "gp"));

        ansiFormat.put("pi.thr",
                String.format("@|faint -%s-+-%s-+-%s-+-%s-+-%s-|@",
                        "-".repeat(id), "-".repeat(q), "-".repeat(50), "-".repeat(w), "-".repeat(gp)));

        ansiFormat.put("pi.tr",
                "@|faint [|@%" + id + "d@|faint ]|@ @|faint (|@%" + q + "d@|faint )|@  %-50s   @|faint |@%" + w
                        + "s@|faint |@   @|faint |@%" + gp + "s@|faint |@%n");
    }

    public static Optional<Long> toLong(String line, boolean warn) {
        try {
            long id = Long.parseLong(line);
            return Optional.of(id);
        } catch (NumberFormatException ignored) {
            if (warn) {
                Term.outPrintf("Unable to determine value from the specified string: %s%n", line);
            }
        }
        return Optional.empty();
    }

    public static double toLongOrDefault(String line, long defaultValue) {
        return line.isBlank()
                ? defaultValue
                : toLong(line, true).orElse(defaultValue);
    }

    public static Optional<Double> toDouble(String line, boolean warn) {
        try {
            return Optional.of(Double.parseDouble(line));
        } catch (NumberFormatException ignored) {
            if (warn) {
                Term.outPrintf("Unable to determine value from the specified string: %s%n", line);
            }
        }
        return Optional.empty();
    }

    public static double toDoubleOrDefault(String line, double defaultValue) {
        return line.isBlank()
                ? defaultValue
                : toDouble(line, true).orElse(defaultValue);
    }

    public static Optional<Double> gpValue(String line, boolean warn) {
        Optional<Double> gpValue = Coinage.gpValue(line);
        if (gpValue.isEmpty() && warn) {
            Term.outPrintf("Unable to determine value from the specified string: %s%n", line);
            Term.outPrintln("A value should be specified as a decimal number and a unit, e.g. 1gp or 0.1pp");
        }
        return gpValue;
    }

    public static double gpValueOrDefault(String line, Double defaultValue) {
        return line.isBlank()
                ? defaultValue
                : gpValue(line, true).orElse(defaultValue);
    }

    public static boolean yesOrTrue(String line, boolean defaultValue) {
        if (line.isBlank()) {
            return defaultValue;
        }
        char first = Character.toLowerCase(line.charAt(0));
        return first == 'y' || first == 't';
    }

    static String howMany(long number, String one, String more) {
        if (number == 0) {
            return "no " + more;
        } else if (number == 1) {
            return number + " " + one;
        }
        return number + " " + more;
    }

    static String weightUnits(double value) {
        if (value == 1.0) {
            return "1 pound";
        } else {
            return value + " pounds";
        }
    }

    static String volumeUnits(double value) {
        if (value == 1.0) {
            return "1 cubic foot";
        } else {
            return value + " cubic feet";
        }
    }

    private static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = slugify = new Slugify()
                    .withCustomReplacement("'", "")
                    .withLowerCase(true);
        }
        return s;
    }

    public static String slugify(String text) {
        return slugifier().slugify(text);
    }
}
