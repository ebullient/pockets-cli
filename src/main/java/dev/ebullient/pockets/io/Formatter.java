package dev.ebullient.pockets.io;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.index.Index;
import dev.ebullient.pockets.index.PocketReference;
import picocli.CommandLine.Help.TextTable;

public class Formatter {

    final PocketTui tui;
    final Map<String, String> ansiFormat = new HashMap<>();

    Index index;
    int pocketIdWidth = 0;

    public Formatter(PocketTui tui) {
        this.tui = tui;
        this.index = new Index(tui); // never null
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public String table(Map<String, String> values) {
        int[] max = {0, 0};
        values.forEach((k, v) -> {
            max[0] = Integer.max(max[0], k.length());
            max[1] = Integer.max(max[1], v.length());
        });

        TextTable table = TextTable.forColumnWidths(tui.colors, max[0] + 2, max[1]);

        values.forEach((k, v) -> table.addRowValues(k, v));
        return table.toString();
    }

    public String listPockets(List<Pocket> pockets) {
        tui.debug(pockets.toString());

        StringBuilder builder = new StringBuilder();
        builder.append("\n 🛍 Your pockets:\n");
        tableOfPockets(builder, pockets);
        builder.append("\n");
        return builder.toString();
    }

    public String describe(Pocket pocket) {
        tui.debug(pocket.toString());

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%n%-2s %s [%d] is empty.%n", getPocketEmoji(pocket), pocket.name, pocket.id));
        builder.append("\n");
        return builder.toString();
    }


    private void tableOfPockets(StringBuilder builder, List<Pocket> pockets) {
        int idWidth = pockets.stream()
                    .filter(p -> p.id != null)
                    .map(p -> p.id.toString().length())
                    .reduce(4, BinaryOperator.maxBy(Integer::max));

        if ( idWidth != pocketIdWidth) {
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

    private String getPocketEmoji(Pocket pocket) {
        PocketReference ref = index.getPocketReference(pocket.pocketRef);
        return getPocketEmoji(pocket, ref);
    }

    private String getPocketEmoji(Pocket pocket, PocketReference ref) {
        tui.debugf("%s %s", pocket.name, ref);
        if ( ref == null ) {
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
}
