package dev.ebullient.pockets;

import java.util.List;
import java.util.Optional;

import org.jline.reader.LineReader;

import com.github.slugify.Slugify;

import dev.ebullient.pockets.db.Pocket;
import picocli.CommandLine.Option;

public class CommonIO {
    private CommonIO() {
    }

    private static Slugify slugify;

    public static Slugify slugifier() {
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

    public static Optional<Long> getId(String nameOrId) {
        try {
            long id = Long.parseLong(nameOrId);
            return Optional.of(id);
        } catch (NumberFormatException ignored) {
        }
        return Optional.empty();
    }

    public static double readOrDefault(String line, double previous) {
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

    public static void listAllPockets() {
        List<Pocket> allPockets = Pocket.listAll();
        Log.outPrintln("\n 🛍  Your pockets:\n");
        Log.outPrintln("@|faint [ ID ]    Name |@");
        Log.outPrintln("@|faint ------+--+----------------------------------------------------+|@");
        //     Log.outPrintln("@|faint ------+--+ 12345678901234567890123456789012345678901234567890 +|@");
        allPockets.forEach(p -> Log.outPrintf("@|faint [|@%4d@|faint ]|@ %-2s %-50s\n", p.id, p.type.icon(), p.name));
        Log.outPrintln("");
    }

    public static void listPocketContents(Pocket pocket) {
        Log.debug(pocket.toString());
        if (pocket.items.isEmpty()) {
            Log.outPrintf("%n%-2s %s [%d] is empty.%n%n", pocket.type.icon(), pocket.name, pocket.id);
            describe(pocket);
        } else {
            Log.outPrintf("%n%-2s %s [%d] contains:%n%n", pocket.type.icon(), pocket.name, pocket.id);
            Log.outPrintln("@|faint [ ID ] (   Q) Description |@");
            Log.outPrintln("@|faint ------+------+----------------------------------------------------+|@");
            //     Log.outPrintln("@|faint ------+------+12345678901234567890123456789012345678901234567890 +|@");
            pocket.items.forEach(
                    i -> Log.outPrintf("@|faint [|@%4d@|faint ] (|@%4d@|faint )|@ %-50s \n", i.id, i.quantity, i.description));
            Log.outPrintln("");
            describe(pocket);
        }
    }

    public static Pocket selectPocketById(Long id) {
        Pocket pocket = Pocket.findById(id);
        if (pocket == null) {
            Log.outPrintf("%n[%s] doesn't match any of your pockets.%n", id);
        }
        return pocket;
    }

    public static Pocket selectPocketByName(String name, LineReader reader) {
        boolean dumb = reader.getTerminal().getType().startsWith("dumb");
        Log.debug(reader.getTerminal().getType());
        List<Pocket> pockets = Pocket.findByName(name);

        if (pockets.size() > 1) {
            Log.outPrintf("%nSeveral pockets match '%s':%n", name);
            Log.outPrintln("@|faint [ ID ]    Name |@");
            Log.outPrintln("@|faint ------+--+-----------------------------------|@");
            pockets.forEach(p -> Log.outPrintf("[%4d] %-2s %s\n", p.id, p.type.icon(), p.name));
            Log.outPrintln("");

            if (!dumb) {
                String line = reader.readLine("Which pocket did you mean [ID, or empty to cancel]? ");
                Optional<Long> newId = getId(line);
                if (newId.isPresent()) {
                    return selectPocketById(newId.get());
                }
            }
            Log.outPrintln("Unable to choose a pocket. Please be more specific.");
        } else if (pockets.size() == 1) {
            return pockets.iterator().next();
        } else {
            Log.outPrintf("%n'%s' doesn't match any of your pockets.%n", name);
            listAllPockets();
        }
        return null;
    }

    public static void describe(Pocket pocket) {
        if (pocket.magic) {
            Log.outPrintf(
                    "@|bold,underline This %s is magical.|@ It always weighs %s pounds, regardless of its contents.%n",
                    pocket.type.prettyName, pocket.weight);
        } else {
            Log.outPrintf("This %s weighs %s pounds when empty.%n", pocket.type.prettyName, pocket.weight);
        }
        if (pocket.magic && pocket.max_capacity == 0) {
            Log.outPrintf("It can hold %s cubic feet of gear.%n", pocket.max_volume);
        } else {
            Log.outPrintf("It can hold %s pounds or %s cubic feet of gear.%n", pocket.max_capacity, pocket.max_volume);
        }
        Log.outPrintln("");
    }

    public static class PocketAttributes {
        @Option(names = { "-c", "--capacity" }, description = "Capacity: Maximum this pocket can contain in pounds")
        Optional<Double> max_capacity = Optional.empty();

        @Option(names = { "-v", "--volume" }, description = "Volume: Maximum this pocket can contain in cubic feet")
        Optional<Double> max_volume = Optional.empty();

        @Option(names = { "-w", "--weight" }, description = "Weight of the pocket itself")
        Optional<Double> weight = Optional.empty();

        @Option(names = { "-m",
                "--magic" }, negatable = true, defaultValue = "false", description = "Is this a magic pocket? Magic pockets always weigh the same, regardless of their contents")
        boolean magic = false;

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
