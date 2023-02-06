package dev.ebullient.pockets.io;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Journal;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketCurrency;
import dev.ebullient.pockets.db.PocketItem;
import dev.ebullient.pockets.db.Posting;
import io.quarkus.qute.TemplateData;
import io.quarkus.qute.TemplateExtension;

@TemplateExtension
@SuppressWarnings("unused")
public class PocketsFormat {
    public static final String PROFILE = "\uD83C\uDFE0"; // 🏠
    public static final String SAVE = "\uD83D\uDCBE"; // 💾
    public static final String BOOM = "\uD83D\uDD25"; // 🔥
    public static final String POCKETS = "\uD83D\uDECD"; // 🛍️
    public static final String ITEM = "\uD83C\uDFF7"; // 🏷️
    public static final String GIFT = "\uD83C\uDF81"; // 🎁
    public static final String LOOK = "\uD83D\uDD0E"; // 🔎
    public static final String MOVE = "\uD83D\uDE9A"; // 🚚
    public static final String BYE = "\uD83D\uDCB8"; // 💸
    public static final String LOOT = "\uD83D\uDCB0"; // 💰
    public static final String CREATE = "✨"; // ✨
    public static final String BOOKS = "\uD83D\uDCDA"; // 📚
    public static final String NBSP = "\u00A0";

    public static final String PROFILE_PREFIX = PROFILE + NBSP + " ";
    public static final String POCKET_PREFIX = POCKETS + NBSP + " ";
    public static final String ITEM_PREFIX = ITEM + NBSP + " ";

    public final static String DOES_NOT_MATCH = "%nThe specified value [%s] doesn't match %s";
    public final static String NOT_FOUND = "%nThe specified id [%s] was not found";
    public final static String MATCHES_MANY = "%nThe specified value [%s] matches more than one %s";
    final static String TABLE_SEP = "  ";

    final static String F_PAREN_OPEN = "@|faint (|@";
    final static String F_PAREN_CLOSE = "@|faint )|@";
    final static String F_SQUARE_OPEN = "@|faint [|@";
    final static String F_SQUARE_CLOSE = "@|faint ]|@";
    final static String PI_QUANTITY = F_PAREN_OPEN + "@|cyan %s|@" + F_PAREN_CLOSE;
    final static String PI_QUANTITY_FIXED = F_PAREN_OPEN + "@|cyan %4s|@" + F_PAREN_CLOSE;
    final static String PI_HEADER_ROW = String.format("@|faint (%4s) %6s  %-60s |@", "Qty", "  ", "[ # ] Name (id)");
    final static String PI_SEPARATOR_ROW = String.format("@|faint %6s %6s  %-60s |@", "-".repeat(6), "-".repeat(6),
            "-".repeat(60));
    final static String PI_TABLE_HEADER = PI_HEADER_ROW + "\n" + PI_SEPARATOR_ROW;
    final static String PI_ROW_FORMAT = PI_QUANTITY_FIXED + " %-6s  %-60s";
    final static String PI_LIST_FORMAT = PI_QUANTITY + " %s";
    final static String NUMBER_NAME_ID_FORMAT = "@|faint [%3s]|@ @|bold %s|@ @|faint (%s)|@";

    public static List<Item> orphans(List<Item> items) {
        return items.stream().filter(i -> i.pocketItems.isEmpty()).collect(Collectors.toList());
    }

    public static String postMemo(Journal journal) {
        return journal.memo == null ? "" : journal.memo.replace("\n", "<br />");
    }

    public static String createMemo(Posting posting, ProfileConfigData pcd) {
        switch (posting.itemType) {
            case POCKET:
                return "Created " + ((Pocket) posting.created).asMemo(pcd).replace("\n", "<br />");
            case ITEM:
                return "Created " + ((Item) posting.created).asMemo(pcd).replace("\n", "<br />");
            default:
                Tui.errorf("Unexpected memo for creating type %s in posting %s", posting.itemType, Transform.toJson(posting));
        }
        return "";
    }

    public static String nameNumberId(Pocket pocket) {
        return String.format(NUMBER_NAME_ID_FORMAT, pocket.id, pocket.name, pocket.slug);
    }

    public static String nameNumberId(Item item) {
        return String.format(NUMBER_NAME_ID_FORMAT, item.id, item.name, item.slug);
    }

    public static String itemPocketHeader(Item item) {
        return PI_TABLE_HEADER;
    }

    public static String itemPocketRow(PocketItem pi) {
        return String.format(PI_ROW_FORMAT, pi.quantity, pi.pocket.flags(), nameNumberId(pi.pocket));
    }

    public static String itemPocketList(Item item) {
        List<String> items = item.pocketItems.stream()
                .map(pi -> String.format(PI_LIST_FORMAT, pi.quantity, nameNumberId(pi.pocket)))
                .collect(Collectors.toList());
        return String.join(", ", items);
    }

    public static String pocketItemHeader(Pocket pocket) {
        return PI_TABLE_HEADER;
    }

    public static String pocketItemRow(PocketItem pi) {
        return String.format(PI_ROW_FORMAT, pi.quantity, pi.item.flags(), nameNumberId(pi.item));
    }

    public static String pocketItemList(Pocket pocket) {
        List<String> items = pocket.pocketItems.values().stream()
                .map(pi -> String.format(PI_LIST_FORMAT, pi.quantity, nameNumberId(pi.item)))
                .collect(Collectors.toList());
        return String.join(", ", items);
    }

    public static String markdownLink(Pocket p) {
        return String.format("[%s](pocket-%s.md)", p.name, p.slug);
    }

    public static String cumulativeValue(List<CurrencyValue> currency, ProfileConfigData cfg) {
        Double cumulativeValue = currency.stream()
                .mapToDouble(c -> c.value)
                .sum();
        return cfg.valueToString(cumulativeValue);
    }

    public static String table(String title, List<String> headings, List<List<String>> values) {
        int[] max = new int[headings.size()];
        String[] colFormat = new String[headings.size()];
        Object[] colFill = new String[headings.size()];
        Arrays.fill(max, 1);

        values.forEach(row -> {
            for (int i = 0; i < headings.size(); i++) {
                max[i] = Integer.max(max[i], row.get(i).length());
            }
        });

        for (int i = 0; i < headings.size(); i++) {
            max[i] = Integer.max(max[i], headings.get(i).length());
            colFormat[i] = "%-" + max[i] + "s";
            colFill[i] = "@|faint " + "-".repeat(max[i]) + "|@";
        }

        String rowFormat = String.join(TABLE_SEP, colFormat) + "%n";

        StringBuilder builder = new StringBuilder();
        builder.append(title).append(":\n\n");
        builder.append(String.format(rowFormat, headings.toArray()));
        builder.append(String.format(rowFormat, colFill));
        values.forEach(row -> builder.append(String.format(rowFormat, row.toArray())));

        return builder.toString();
    }

    public static void showProfiles(Map<String, ProfileConfigData> map) {
        Tui.println(table(PROFILE_PREFIX + "Profiles defined",
                List.of("ID", "Description"),
                map.values().stream()
                        .map(x -> List.of(x.slug, Transform.toStringOrEmpty(x.description)))
                        .collect(Collectors.toList())));
    }

    public static void showPockets(Collection<Pocket> allPockets) {
        Tui.println(POCKET_PREFIX + "Defined pockets:");
        Tui.println("@|faint [ # ] Name (id)|@");
        allPockets.forEach(p -> Tui.printlnf("%s %s",
                nameNumberId(p), Transform.toStringOrEmpty(p.pocketDetails.notes)));
    }

    public static void showItems(Collection<Item> allItems) {
        Tui.println(POCKET_PREFIX + "Defined items:");
        Tui.println("@|faint [ # ] Name (id)|@");
        allItems.forEach(i -> Tui.printlnf("%s %s",
                nameNumberId(i), Transform.toStringOrEmpty(i.itemDetails.notes)));
    }

    @TemplateData
    public static class CurrencyValue {
        public final String name;
        public final String notation;
        public final double unitConversion;

        public long quantity;
        public double value;

        public List<PocketCurrency> pocketCurrencies = new ArrayList<>();

        public CurrencyValue(PocketCurrency pc) {
            this.name = pc.name;
            this.notation = pc.currency;
            this.unitConversion = pc.unitConversion;
        }

        public void add(PocketCurrency pc) {
            quantity += pc.quantity;
            value = quantity * unitConversion;
            pocketCurrencies.add(pc);
        }

        public String getPockets() {
            return pocketCurrencies.stream()
                    .sorted(Comparator.comparing(pc -> pc.pocket.name))
                    .map(pc -> String.format("[%s](pocket-%s.md)&nbsp;(%s)", pc.pocket.name, pc.pocket.slug, pc.quantity))
                    .collect(Collectors.joining(", "));
        }

        @Override
        public String toString() {
            return "CurrencyValue{" + "name='" + name + '\'' +
                    ", notation='" + notation + '\'' +
                    ", unitConversion=" + unitConversion +
                    ", quantity=" + quantity +
                    ", unitValue=" + value +
                    ", pocketCurrencies=" + pocketCurrencies +
                    '}';
        }
    }
}
