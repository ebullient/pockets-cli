package dev.ebullient.pockets;

import java.util.List;
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

    public static Optional<Long> toLong(String nameOrId) {
        try {
            long id = Long.parseLong(nameOrId);
            return Optional.of(id);
        } catch (NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    public static double toDoubleOrDefault(String line, double previous) {
        if (!line.isBlank()) {
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException ignored) {
            }
        }
        return previous;
    }

    public static boolean yesOrTrue(String line, boolean defaultValue) {
        if (!line.isBlank()) {
            char first = Character.toLowerCase(line.charAt(0));
            return first == 'y' || first == 't';
        }
        return defaultValue;
    }

    static String howMany(long number, String one, String more) {
        if (number == 0) {
            return "no " + more;
        } else if (number == 1) {
            return number + " " + one;
        }
        return number + " " + more;
    }

    public static void dumpStatistics() {
        long pockets = Pocket.count();
        long pocketitem = PocketItem.count();
        Term.outPrintf("%nYou have %s containing %s.%n",
                howMany(pockets, "pocket", "pockets"),
                howMany(pocketitem, "pocket item", "pocket items"));
    }

    public static void listAllPockets() {
        List<Pocket> allPockets = Pocket.listAll();
        Term.outPrintln("\n 🛍  Your pockets:\n");
        Term.outPrintln("@|faint [ ID ]    Name |@");
        Term.outPrintln("@|faint ------+--+----------------------------------------------------+|@");
        //     Log.outPrintln("@|faint ------+--+ 12345678901234567890123456789012345678901234567890 +|@");
        allPockets.forEach(p -> Term.outPrintf("@|faint [|@%4d@|faint ]|@ %-2s %-50s\n", p.id, p.type.icon(), p.name));
        Term.outPrintln("");
    }

    public static void listPocketContents(Pocket pocket) {
        Term.debug(pocket.toString());
        if (pocket.items.isEmpty()) {
            Term.outPrintf("%n%-2s %s [%d] is empty.%n%n", pocket.type.icon(), pocket.name, pocket.id);
            describe(pocket);
        } else {
            Term.outPrintf("%n%-2s %s [%d] contains:%n%n", pocket.type.icon(), pocket.name, pocket.id);
            Term.outPrintln("@|faint [ ID ] (   Q) Description |@");
            Term.outPrintln("@|faint ------+------+----------------------------------------------------+|@");
            //     Log.outPrintln("@|faint ------+------+12345678901234567890123456789012345678901234567890 +|@");
            pocket.items.forEach(
                    i -> Term.outPrintf("@|faint [|@%4d@|faint ] (|@%4d@|faint )|@ %-50s \n", i.id, i.quantity, i.description));
            Term.outPrintln("");
            describe(pocket);
        }
    }

    public static Pocket selectPocketById(Long id) {
        Pocket pocket = Pocket.findById(id);
        if (pocket == null) {
            Term.outPrintf("%n[%s] doesn't match any of your pockets.%n", id);
        }
        return pocket;
    }

    public static Pocket selectPocketByName(String name) {
        List<Pocket> pockets = Pocket.findByName(name);

        if (pockets.size() > 1) {
            Term.outPrintf("%nSeveral pockets match '%s':%n", name);
            Term.outPrintln("@|faint [ ID ]    Name |@");
            Term.outPrintln("@|faint ------+--+-----------------------------------|@");
            pockets.forEach(p -> Term.outPrintf("[%4d] %-2s %s\n", p.id, p.type.icon(), p.name));
            Term.outPrintln("");

            if (Term.canPrompt()) {
                String line = Term.prompt("Which pocket did you mean [ID, or empty to cancel]? ");
                Optional<Long> newId = toLong(line);
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

    public static void describe(Pocket pocket) {
        if (pocket.magic) {
            Term.outPrintf(
                    "@|bold,underline This %s is magical.|@ It always weighs %s pounds, regardless of its contents.%n",
                    pocket.type.prettyName, pocket.weight);
        } else {
            Term.outPrintf("This %s weighs %s pounds when empty.%n", pocket.type.prettyName, pocket.weight);
        }

        if (pocket.magic && pocket.max_capacity == 0) {
            Term.outPrintf("It can hold %s cubic feet of gear.%n", pocket.max_volume);
        } else {
            Term.outPrintf("It can hold %s pounds or %s cubic feet of gear.%n", pocket.max_capacity, pocket.max_volume);
        }

        Term.outPrintln("");
    }

    public static class PocketAttributes {
        @Option(names = { "-w", "--max-weight" }, description = "Maximum weight of this pocket in pounds")
        Optional<Double> max_capacity = Optional.empty();

        @Option(names = { "-v", "--max-volume" }, description = "Maximum volume of this pocket in cubic feet")
        Optional<Double> max_volume = Optional.empty();

        @Option(names = { "-p", "--weight" }, description = "Weight of the pocket itself in pounds")
        Optional<Double> weight = Optional.empty();

        @Option(names = { "-m",
                "--magic" }, negatable = true, defaultValue = "false", description = "Is this a magic pocket?%n  Magic pockets always weigh the same, regardless of their contents")
        boolean magic = false;

        boolean isComplete() {
            return max_capacity.isPresent() && max_volume.isPresent() && weight.isPresent();
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "max_capacity=" + max_capacity +
                    ", max_volume=" + max_volume +
                    ", weight=" + weight +
                    ", magic=" + magic +
                    '}';
        }
    }

    static class ItemAttributes {
        @Option(names = { "-q", "--quantity" }, description = "Quantity of items to add", defaultValue = "1")
        int quantity = 1;

        @Option(names = { "-w", "--weight" }, description = "Weight of a single item in pounds")
        Optional<Double> weight;

        @Option(names = { "-v",
                "--value" }, description = "Value of a single item. Specify units (gp, ep, sp, cp)")
        Optional<String> gpValue;

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + "{" +
                    "quantity=" + quantity +
                    ", weight=" + weight +
                    ", gpValue=" + gpValue +
                    '}';
        }
    }
}
