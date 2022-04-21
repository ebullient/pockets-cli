package dev.ebullient.pockets.io;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import dev.ebullient.pockets.db.Mapper;

public class Reader implements AutoCloseable {

    private final LineReader reader;
    private final PocketTui tui;

    Reader(boolean interactive, PocketTui tui) {
        this.tui = tui;

        LineReader r = null;
        if (interactive) {
            r = LineReaderBuilder.builder().build();
            if (r.getTerminal().getType().startsWith("dumb")) {
                tryToClose(r);
                String message = "Interactive mode requested, but not using an interactive terminal.";
                tui.error(message);
                throw new RuntimeException(message);
            } else {
                tui.out = r.getTerminal().writer();
            }
        }
        this.reader = r;
    }

    public String prompt(String prompt) {
        if (reader == null) {
            return "";
        }

        try {
            return reader.readLine("\n🔷 " + prompt);
        } catch (org.jline.reader.UserInterruptException ex) {
            System.exit(3);
            return "";
        }
    }

    public boolean confirm() {
        return confirm("Save your changes");
    }

    public boolean confirm(String prompt) {
        if (reader == null) {
            return false;
        }

        try {
            String line = reader.readLine("\n🔷 " + prompt + " (Y|n)? ");
            return Mapper.toBooleanOrDefault(line, true);
        } catch (org.jline.reader.UserInterruptException ex) {
            System.exit(3);
            return false;
        }
    }

    @Override
    public void close() {
        tryToClose(this.reader);
    }

    private void tryToClose(LineReader reader) {
        if (reader != null) {
            try {
                reader.getTerminal().close();
            } catch (IOException e) {
                tui.error(e, "Unable to close terminal");
            }
        }
    }
}
