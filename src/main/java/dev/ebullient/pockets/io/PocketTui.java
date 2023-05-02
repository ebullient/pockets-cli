package dev.ebullient.pockets.io;

import static dev.ebullient.pockets.io.PocketsFormat.*;

import java.io.PrintWriter;
import java.util.Arrays;

import dev.ebullient.pockets.Transform;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.CommandSpec;

public class PocketTui {
    public static final PocketTui Tui = new PocketTui();
    public static final int NOT_FOUND = 3;
    public static final int INSUFFICIENT_FUNDS = 4;
    public static final int CANCELED = 5;
    public static final int CONFLICT = 6;
    public static final int BAD_DATA = 7;

    static final String DEBUG = "\uD83D\uDD27\u00A0 "; // 🔧
    static final String DONE = "\u2705\u00A0 "; // ✅
    static final String ERROR = "\uD83D\uDED1\u00A0 ";// 🛑
    static final String INFO = "\uD83D\uDD39\u00A0 "; // 🔹
    static final String WARN = "\uD83D\uDD38\u00A0 "; // 🔸
    static final String PROMPT = "\n\uD83D\uDDF3\u00A0 "; // 🗳️
    static final String CREATE_MSG = CREATE + NBSP + " ";

    Ansi ansi;
    ColorScheme colors;

    PrintWriter out;
    PrintWriter err;

    private boolean debugEnabled;
    private boolean verboseEnabled;
    private boolean interactive;

    private Reader reader;

    public PocketTui() {
        this.ansi = Help.Ansi.OFF;
        this.colors = Help.defaultColorScheme(ansi);

        this.out = new PrintWriter(System.out);
        this.err = new PrintWriter(System.err);
        this.debugEnabled = false;
        this.verboseEnabled = true;
        this.interactive = false;
    }

    public void init(boolean debugEnabled, boolean verboseEnabled) {
        this.debugEnabled = debugEnabled;
        this.verboseEnabled = verboseEnabled;
    }

    public void init(CommandSpec spec, InputOutputOptionsMixin ioOptions) {
        this.ansi = spec.commandLine().getHelp().ansi();
        this.colors = spec.commandLine().getHelp().colorScheme();

        this.out = spec.commandLine().getOut();
        this.err = spec.commandLine().getErr();
        this.debugEnabled = ioOptions.debug;
        this.verboseEnabled = !ioOptions.quiet;
        this.interactive = ioOptions.interactive;
    }

    public void close() {
        out.flush();
        if (reader != null) {
            reader.close();
        }
        err.flush();
    }

    public void createf(String format, Object... params) {
        if (verboseEnabled) {
            create(String.format(format, params));
        }
    }

    public void create(String output) {
        if (verboseEnabled) {
            println(CREATE_MSG + output);
        }
    }

    public void debugf(String format, Object... params) {
        if (debugEnabled) {
            debug(String.format(format, params));
        }
    }

    public void debug(String output) {
        if (debugEnabled) {
            Arrays.stream(output.split("\n"))
                    .forEach(l -> out.println(ansi.new Text(DEBUG + "@|faint " + l + "|@", colors)));
            out.flush();
        }
    }

    public void donef(String format, Object... params) {
        if (verboseEnabled) {
            done(String.format(format, params));
        }
    }

    public void done(String output) {
        if (verboseEnabled) {
            println(DONE + output);
        }
    }

    public void errorf(String format, Object... args) {
        error(null, String.format(format, args));
    }

    public void errorf(Throwable th, String format, Object... args) {
        error(th, String.format(format, args));
    }

    public void error(InvalidPocketState pocketsError) {
        if (pocketsError.showMessage()) {
            error(pocketsError.ipsOrCause(), pocketsError.getMessage());
        }
    }

    public void error(String errorMsg) {
        error(null, errorMsg);
    }

    public void error(Throwable ex, String errorMsg) {
        err.println(ansi.new Text(ERROR + "@|fg(red) " + errorMsg + "|@", colors));
        if (ex != null && debugEnabled) {
            ex.printStackTrace(err);
        }
        err.flush();
    }

    public void infof(String format, Object... params) {
        info(String.format(format, params));
    }

    public void info(String output) {
        println(INFO + output);
    }

    public void printf(String format, Object... args) {
        String output = String.format(format, args);
        out.print(ansi.new Text(output, colors));
        out.flush();
    }

    public void printlnf(String format, Object... args) {
        println(String.format(format, args));
    }

    public void println(String output) {
        out.println(ansi.new Text(output, colors));
        out.flush();
    }

    public void println(String... output) {
        Arrays.stream(output).forEach(l -> out.println(ansi.new Text(l, colors)));
        out.flush();
    }

    public boolean isVerboseEnabled() {
        return verboseEnabled;
    }

    public void warnf(String format, Object... params) {
        warn(String.format(format, params));
    }

    public void warn(String output) {
        println(WARN + output);
    }

    public boolean interactive() {
        return interactive;
    }

    public String promptIfMissing(String prompt, String value) {
        if (interactive && Transform.isBlank(value)) {
            value = reader().prompt(PROMPT + prompt);
        }
        return value;
    }

    public String promptForValueWithFallback(String prompt, String fallback) {
        String value = reader().prompt(String.format("%s%s [%s]", PROMPT, prompt, fallback));
        return Transform.isBlank(value) ? fallback : value;
    }

    public boolean confirm(String prompt) {
        return confirm(prompt, false);
    }

    public boolean confirm(String prompt, boolean destructive) {
        try {
            String prefix = destructive ? BOOM + PROMPT : PROMPT;

            String line = reader().prompt(prefix + prompt + " (Y|n)? ");
            return Transform.toBooleanOrDefault(line, true);
        } catch (org.jline.reader.UserInterruptException ex) {
            System.exit(CANCELED);
            return false;
        }
    }

    public boolean booleanValue(String prompt, boolean defaultValue) {
        try {
            String suffix = defaultValue ? " (Y|n)? " : " (y|N)? ";
            String line = reader().prompt(PROMPT + prompt + suffix);
            return Transform.toBooleanOrDefault(line, defaultValue);
        } catch (org.jline.reader.UserInterruptException ex) {
            System.exit(CANCELED);
            return defaultValue;
        }
    }

    public Reader reader() {
        if (reader == null) {
            reader = new Reader(interactive(), this);
        }
        return reader;
    }

    public PrintWriter err() {
        return err;
    }
}
