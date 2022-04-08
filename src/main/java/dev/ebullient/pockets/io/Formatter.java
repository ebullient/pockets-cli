package dev.ebullient.pockets.io;

import java.util.Map;

import picocli.CommandLine.Help.TextTable;

public class Formatter {

    PocketTui tui;

    public Formatter(PocketTui tui) {
        this.tui = tui;
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


}
