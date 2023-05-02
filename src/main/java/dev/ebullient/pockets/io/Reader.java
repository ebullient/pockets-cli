package dev.ebullient.pockets.io;

import java.io.IOException;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import picocli.CommandLine.ExitCode;

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
                throw new InvalidPocketState(ExitCode.USAGE,
                        "Interactive mode requested, but not using an interactive terminal.");
            } else {
                tui.out = r.getTerminal().writer();
            }
        }
        this.reader = r;
    }

    String prompt(String prompt) {
        if (reader == null) {
            return "";
        }
        try {
            return reader.readLine(prompt);
        } catch (org.jline.reader.UserInterruptException ex) {
            System.exit(PocketTui.CANCELED);
            return "";
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
                tui.errorf(e, "Unable to close terminal");
            }
        }
    }
}
