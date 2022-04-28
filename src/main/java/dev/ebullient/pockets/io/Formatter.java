package dev.ebullient.pockets.io;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import dev.ebullient.pockets.db.Currency;
import dev.ebullient.pockets.db.Currency.CoinPurse;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.index.PocketReference;

public class Formatter {

    final PocketTui tui;
    final Map<String, String> ansiFormat = new HashMap<>();

    Index index;
    int pocketIdWidth = 0;
    int[] itemWidths = { 0, 0, 0, 0 };

    public Formatter(PocketTui tui) {
        this.tui = tui;
        this.index = new Index(tui); // never null
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public String table(String header1, String header2, Map<String, String> values) {
        int[] max = { 0, 0 };
        values.forEach((k, v) -> {
            max[0] = Integer.max(max[0], k.length());
            max[1] = Integer.max(max[1], v.length());
        });
        String format = "%-" + max[0] + "s @|faint ||@ %-" + max[1] + "s%n";
        StringBuilder builder = new StringBuilder();

        builder.append(String.format(format, header1, header2));
        builder.append(String.format(format,
                "@|faint " + "-".repeat(max[0]) + "|@",
                "@|faint " + "-".repeat(max[1]) + "|@"));

        values.forEach((k, v) -> builder.append(String.format(format, k, v)));

        return builder.toString();
    }

    public String listPockets(List<Pocket> pockets) {
        tui.debug(pockets.toString());

        StringBuilder builder = new StringBuilder();
        builder.append("\n 🛍 Your pockets:\n");
        appendTableOfPockets(builder, pockets);
        builder.append("\n");
        return builder.toString();
    }

    public String describe(Pocket pocket) {
        return describe(pocket, true);
    }

    public String describe(Pocket pocket, boolean details) {
        PocketReference ref = index.getPocketReference(pocket.pocketRef);
        tui.debug(pocket.toString());

        StringBuilder builder = new StringBuilder();

        if (pocket.items == null || pocket.items.isEmpty()) {
            builder.append(String.format("%n%-2s %s [%d] is empty.%n", getPocketEmoji(pocket, ref), pocket.name, pocket.id));
        } else {
            builder.append(String.format("%n%-2s %s [%d] contains:%n%n", getPocketEmoji(pocket, ref), pocket.name, pocket.id));
            appendTableOfItems(builder, pocket.items);
        }
        builder.append("\n");

        if (details) {
            if (pocket.extradimensional) {
                builder.append(String.format(
                        "@|bold,underline This %s is magical.|@%nIt always weighs %s, regardless of its contents.%n",
                        ref.name, weightUnits(pocket.weight)));
            } else {
                builder.append(String.format("This %s weighs %s when empty.%n", ref.name, weightUnits(pocket.weight)));
            }

            if (pocket.notes != null) {
                builder.append(String.format("🔖 %s%n", pocket.notes));
            }

            String weight = "";
            if (pocket.max_weight != null && pocket.max_weight != 0) {
                weight = weightUnits(pocket.max_weight);
            }
            String volume = "";
            if (pocket.max_volume != null && pocket.max_volume != 0) {
                volume = volumeUnits(pocket.max_volume);
            }
            builder.append(String.format("⚖️ It can hold %s%s%s of gear.%n",
                    weight,
                    (weight.length() > 0 && volume.length() > 0) ? " or " : "",
                    volume));

            if (ref.hasConstraints()) {
                builder.append(ref.describeConstraints());
            }
        }

        return builder.toString();
    }

    public String describe(Item item) {
        StringBuilder builder = new StringBuilder();
        builder.append("  Name         : ").append(item.name).append("\n");
        builder.append("  Quantity     : ").append(item.quantity).append("\n");
        builder.append("  Weight (lbs) : ").append((item.weight == null ? "unknown" : item.weight)).append("\n");
        builder.append("  Value (gp)   : ").append((item.cpValue == null ? "unknown" : item.cpValue * Currency.cp.gpEx))
                .append("\n");
        builder.append("  Tradable     : ").append(item.tradable).append("\n");
        return builder.toString();
    }

    public String describe(Pocket pocket, CoinPurse coinPurse) {
        PocketReference ref = index.getPocketReference(pocket.pocketRef);

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%n%-2s %s [%d] contains:%n%n", getPocketEmoji(pocket, ref), pocket.name, pocket.id));
        appendTableOfItems(builder, coinPurse.collectItems());

        return builder.toString();
    }

    public String howMany(long number, String one, String more) {
        if (number == 0) {
            return "no " + more;
        } else if (number == 1) {
            return number + " " + one;
        }
        return number + " " + more;
    }

    private void appendTableOfPockets(StringBuilder builder, List<Pocket> pockets) {
        int idWidth = pockets.stream()
                .filter(p -> p.id != null)
                .map(p -> p.id.toString().length())
                .reduce(4, BinaryOperator.maxBy(Integer::max));

        if (idWidth != pocketIdWidth) {
            setPocketTableFormat(idWidth);
        }

        builder.append(ansiFormat.get("p.th"));
        builder.append(ansiFormat.get("p.thr"));
        pockets.forEach(
                p -> builder.append(String.format(ansiFormat.get("p.tr"),
                        p.id, getPocketEmoji(p),
                        p.extradimensional ? "@|fg(magenta) *|@" : " ",
                        p.name)));
    }

    private void appendTableOfItems(StringBuilder builder, Collection<Item> items) {
        int[] widths = { 4, 3, 4, 4 };
        items.forEach(i -> {
            widths[0] = Math.max(widths[0], valueToString(i.id).length());
            widths[1] = Math.max(widths[1], valueToString(i.quantity).length());
            widths[2] = Math.max(widths[2], valueToString(i.weight).length());
            widths[3] = Math.max(widths[3], valueToString(i.cpValue).length());
        });

        if (!Arrays.equals(itemWidths, widths)) {
            setItemTableFormat(widths);
        }

        builder.append(ansiFormat.get("pi.th"));
        builder.append(ansiFormat.get("pi.thr"));
        items.forEach(
                i -> {
                    // Display items that have a quantity > 0. Show all in verbose mode
                    if (i.quantity > 0 || tui.isVerbose()) {
                        builder.append(String.format(ansiFormat.get("pi.tr"),
                                i.id, i.quantity, i.name,
                                i.weight == null ? "-" : i.weight,
                                i.cpValue == null ? "-" : i.cpValue * Currency.cp.gpEx, // display in gp
                                i.tradable ? " " : "🔒"));
                    }
                });
    }

    private String getPocketEmoji(Pocket pocket) {
        PocketReference ref = index.getPocketReference(pocket.pocketRef);
        return getPocketEmoji(pocket, ref);
    }

    private String getPocketEmoji(Pocket pocket, PocketReference ref) {
        tui.debugf("%s %s", pocket.name, ref);
        if (ref == null) {
            return PocketReference.emojiForSlug(pocket.slug);
        }
        String emoji = ref.getEmoji();
        if (emoji == null) {
            emoji = ref.setEmoji(PocketReference.emojiForSlug(ref.idSlug));
        }
        return emoji;
    }

    private void setPocketTableFormat(int idWidth) {
        ansiFormat.put("p.th",
                String.format("@|faint [%" + idWidth + "s]     %-50s |@%n", "ID ", "Name"));

        ansiFormat.put("p.thr",
                String.format("@|faint -%s-|--|-%s-|@%n", "-".repeat(idWidth), "-".repeat(50)));

        ansiFormat.put("p.tr", "@|faint [|@%" + idWidth + "d@|faint ]|@ %-2s%s %-50s%n");

        pocketIdWidth = idWidth;
    }

    private void setItemTableFormat(int[] widths) {
        int id = widths[0];
        int q = widths[1];
        int w = widths[2];
        int gp = widths[3];

        ansiFormat.put("pi.th",
                String.format("@|faint [%" + id + "s] (%" + q + "s)  %-50s   %" + w + "s   %" + gp + "s  t|@%n",
                        "ID ", "Q ", "Name / Description", "lbs", "gp"));

        ansiFormat.put("pi.thr",
                String.format("@|faint -%s-|-%s-|-%s-|-%s-|-%s-|-|@%n",
                        "-".repeat(id), "-".repeat(q), "-".repeat(50), "-".repeat(w), "-".repeat(gp)));

        ansiFormat.put("pi.tr",
                "@|faint [|@%" + id + "d@|faint ]|@ @|faint (|@%" + q + "d@|faint )|@  %-50s   %" + w
                        + "s   %" + gp + "s  %s%n");

        itemWidths = widths;
    }

    private String weightUnits(double value) {
        if (value == 1.0) {
            return "1 pound";
        } else {
            return value + " pounds";
        }
    }

    private String volumeUnits(double value) {
        if (value == 1.0) {
            return "1 cubic foot";
        } else {
            return value + " cubic feet";
        }
    }

    private String valueToString(Object o) {
        return o == null ? "" : o.toString();
    }
}
