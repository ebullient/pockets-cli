package dev.ebullient.pockets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.slugify.Slugify;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import picocli.CommandLine.Option;

/**
 * Common output formatting and data manipulation for either parameters
 * or return types. Common across many commands.
 */
public class CommonIO {
    private CommonIO() {
    }

    static Optional<Map<String, Integer>> fieldWidths = Optional.empty();
    private static Slugify slugify;

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

    static String ansiId(Long id) {
        return Term.ansiPrintf("@|faint [|@%" + idWidth() + "d@|faint ]|@", id);
    }

    static String ansiQuantity(int quantity) {
        return Term.ansiPrintf("@|faint (|@%" + quantityWidth() + "d@|faint )|@", quantity);
    }

    static String ansiWeight(Double weight) {
        return Term.ansiPrintf("@|faint |@%" + weightWidth() + "s@|faint |@", weight == null ? "-" : weight);
    }

    static String ansiValue(Double value) {
        return Term.ansiPrintf("@|faint |@%" + valueWidth() + "s@|faint |@", value == null ? "-" : value);
    }

    public static int idWidth() {
        if (fieldWidths.isEmpty()) {
            fieldWidths = Optional.of(PocketItem.fieldWidths());
        }
        return fieldWidths.get().get("id");
    }

    public static int quantityWidth() {
        if (fieldWidths.isEmpty()) {
            fieldWidths = Optional.of(PocketItem.fieldWidths());
        }
        return fieldWidths.get().get("quantity");
    }

    public static int weightWidth() {
        if (fieldWidths.isEmpty()) {
            fieldWidths = Optional.of(PocketItem.fieldWidths());
        }
        return fieldWidths.get().get("weight");
    }

    public static int valueWidth() {
        if (fieldWidths.isEmpty()) {
            fieldWidths = Optional.of(PocketItem.fieldWidths());
        }
        return fieldWidths.get().get("value");
    }

    static void printPockets(List<Pocket> pockets) {
        Term.outPrintf("@|faint [%" + idWidth() + "s]    %-50s |@%n", "ID ", "Name");
        Term.outPrintf("@|faint -%s-+--+-%s-|@%n", "-".repeat(idWidth()), "-".repeat(50));
        pockets.forEach(p -> Term.outPrintf("%s %-2s %-50s%n", ansiId(p.id), p.type.icon(), p.name));
    }

    static void printPocketItems(Collection<PocketItem> items) {
        Term.outPrintf(
                "@|faint [%" + idWidth() + "s] (%" + quantityWidth() + "s)  %-50s   %" + weightWidth() + "s   %" + valueWidth()
                        + "s|@%n",
                "ID ", "Q ", "Name / Description", "lbs", "gp");
        Term.outPrintf("@|faint -%s-+-%s-+-%s-+-%s-+-%s-|@%n",
                "-".repeat(idWidth()), "-".repeat(quantityWidth()), "-".repeat(50), "-".repeat(weightWidth()),
                "-".repeat(valueWidth()));
        items.forEach(
                i -> Term.outPrintf("%s %s  %-50s   %s   %s\n", ansiId(i.id), ansiQuantity(i.quantity), i.name,
                        ansiWeight(i.weight), ansiValue(i.value)));
    }

    public static void dumpStatistics() {
        long pockets = Pocket.count();
        long pocketitem = PocketItem.count();
        Term.outPrintf("%nYou have %s containing %s.%n",
                howMany(pockets, "pocket", "pockets"),
                howMany(pocketitem, "pocket item", "pocket items"));
    }

    public static void listAllPockets() {
        Term.outPrintln("\n 🛍  Your pockets:\n");
        printPockets(Pocket.listAll());
        Term.outPrintln("");
    }

    public static void listPocketContents(Pocket pocket) {
        Term.debug(pocket.toString());
        if (pocket.items.isEmpty()) {
            Term.outPrintf("%n%-2s %s [%d] is empty.%n%n", pocket.type.icon(), pocket.name, pocket.id);
            describe(pocket);
        } else {
            Term.outPrintf("%n%-2s %s [%d] contains:%n%n", pocket.type.icon(), pocket.name, pocket.id);
            printPocketItems(pocket.items);
            Term.outPrintln("");
            describe(pocket);
        }
    }

    public static Pocket selectPocketById(Long id) {
        Pocket pocket = Pocket.findById(id);
        if (pocket == null) {
            Term.outPrintf("%nThe specified value [%s] doesn't match any of your pockets.%n", id);
            listAllPockets();
            return null;
        }
        return pocket;
    }

    public static PocketItem selectPocketItemById(Pocket pocket, Long item_id) {
        PocketItem item = PocketItem.findById(item_id);
        if (item == null || !item.belongsTo(pocket)) {
            Term.outPrintf("%nThe specified value [%s] doesn't match any of the items in this pocket.%n", item_id);
            return null;
        }
        return item;
    }

    public static Pocket selectPocketByName(String name) {
        List<Pocket> pockets = Pocket.findByName(name);

        if (pockets.size() > 1) {
            Term.outPrintf("%nSeveral pockets match '%s':%n", name);
            printPockets(pockets);
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

    public static PocketItem selectPocketItemByName(Pocket pocket, String name) {
        List<PocketItem> items = PocketItem.findByName(pocket, name);

        if (items.size() > 1) {
            Term.outPrintf("%nSeveral items match '%s':%n", name);
            printPocketItems(items);
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

    public static void describe(Pocket pocket) {
        if (pocket.magic) {
            Term.outPrintf(
                    "@|bold,underline This %s is magical.|@%nIt always weighs %s, regardless of its contents.%n",
                    pocket.type.prettyName, weightUnits(pocket.weight));
        } else {
            Term.outPrintf("This %s weighs %s when empty.%n", pocket.type.prettyName, weightUnits(pocket.weight));
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

        if (pocket.comments != null && !pocket.comments.isBlank()) {
            Term.outPrintln(pocket.comments);
        }
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

    public static class PocketAttributes {
        @Option(names = { "-w", "--max-weight" }, description = "Maximum weight of this pocket in pounds")
        Optional<Double> max_weight = Optional.empty();

        @Option(names = { "-v", "--max-volume" }, description = "Maximum volume of this pocket in cubic feet")
        Optional<Double> max_volume = Optional.empty();

        @Option(names = { "-p", "--weight" }, description = "Weight of the pocket itself in pounds")
        Optional<Double> weight = Optional.empty();

        @Option(names = { "-m",
                "--magic" }, negatable = true, defaultValue = "false", description = "Is this a magic pocket?%n  Magic pockets always weigh the same, regardless of their contents")
        boolean magic = false;

        @Option(names = { "-c", "--comments" }, description = "Comments or constraints")
        Optional<String> comments;

        public PocketAttributes() {
        }

        boolean isComplete() {
            return max_weight.isPresent() && max_volume.isPresent() && weight.isPresent();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "max_weight=" + max_weight +
                    ", max_volume=" + max_volume +
                    ", weight=" + weight +
                    ", magic=" + magic +
                    ", comments=" + comments +
                    '}';
        }
    }

    public static void describe(PocketItem item) {
        Term.outPrintln("  Quantity     : " + item.quantity);
        Term.outPrintln("  Weight (lbs) : " + (item.weight == null ? "unknown" : item.weight));
        Term.outPrintln("  Value (gp)   : " + (item.value == null ? "unknown" : item.value));
    }

    static class ItemAttributes {
        @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add", defaultValue = "1")
        int quantity = 1;

        @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds")
        Optional<Double> weight = Optional.empty();

        @Option(names = { "-v",
                "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp)")
        Optional<String> value = Optional.empty();

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "quantity=" + quantity +
                    ", weight=" + weight +
                    ", value=" + value +
                    '}';
        }
    }
}
