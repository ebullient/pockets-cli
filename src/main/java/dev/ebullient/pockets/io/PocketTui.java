package dev.ebullient.pockets.io;

import java.io.PrintWriter;

import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.CommandSpec;

public class PocketTui {
    static final boolean picocliDebugEnabled = "DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"));

    public static final int NOT_FOUND = 3;

    Ansi ansi;
    ColorScheme colors;

    PrintWriter out;
    PrintWriter err;

    private boolean debug;
    private boolean verbose;
    private boolean interactive;

    private Reader reader;
    private final Formatter formatter = new Formatter(this);

    public PocketTui() {
        this.ansi = Help.Ansi.OFF;
        this.colors = Help.defaultColorScheme(ansi);

        this.out = new PrintWriter(System.out);
        this.err = new PrintWriter(System.err);
        this.debug = false;
        this.verbose = true;
        this.interactive = false;
    }

    public void init(CommandSpec spec, boolean debug, boolean verbose, boolean interactive) {
        this.ansi = spec.commandLine().getHelp().ansi();
        this.colors = spec.commandLine().getHelp().colorScheme();

        this.out = spec.commandLine().getOut();
        this.err = spec.commandLine().getErr();
        this.debug = debug;
        this.verbose = verbose;
        this.interactive = interactive;
    }

    public void close() {
        out.flush();
        if (reader != null) {
            reader.close();
        }
        err.flush();
    }

    public Formatter format() {
        return formatter;
    }

    public boolean isDebug() {
        return debug || picocliDebugEnabled;
    }

    public void debugf(String format, Object... params) {
        if (isDebug()) {
            debug(String.format(format, params));
        }
    }

    public void debug(String output) {
        if (isDebug()) {
            out.println(ansi.new Text("@|faint " + output + "|@", colors));
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void verbosef(String format, Object... params) {
        if (isVerbose()) {
            outPrintf(format, params);
        }
    }

    public void verbose(String output) {
        if (isVerbose()) {
            outPrintln(output);
        }
    }

    public void warnf(String format, Object... params) {
        warn(String.format(format, params));
    }

    public void warn(String output) {
        out.println(ansi.new Text("🔸 " + output));
    }

    public void donef(String format, Object... params) {
        done(String.format(format, params));
    }

    public void done(String output) {
        out.println(ansi.new Text("✅ " + output));
    }

    public void createf(String format, Object... params) {
        create(String.format(format, params));
    }

    public void create(String output) {
        out.println(ansi.new Text("✨ " + output));
    }

    public void outPrintf(String format, Object... args) {
        String output = String.format(format, args);
        out.print(ansi.new Text(output, colors));
        out.flush();
    }

    public void outPrintln(String output) {
        out.println(ansi.new Text(output));
        out.flush();
    }

    public void errorf(String format, Object... args) {
        error(null, String.format(format, args));
    }

    public void errorf(Throwable th, String format, Object... args) {
        error(th, String.format(format, args));
    }

    public void error(String errorMsg) {
        error(null, errorMsg);
    }

    public void error(Throwable ex, String errorMsg) {
        err.println(ansi.new Text("🛑 @|fg(red) " + errorMsg + "|@", colors));
        if (ex != null && isDebug()) {
            ex.printStackTrace(err);
        }
        err.flush();
    }

    public void showUsage(CommandSpec spec) {
        spec.commandLine().usage(out, ansi);
    }

    public void errShowUsage(CommandSpec spec) {
        spec.commandLine().usage(err, ansi);
    }

    public boolean interactive() {
        return interactive;
    }

    public Reader reader() {
        if (interactive && reader == null) {
            reader = new Reader(interactive, this);
        }
        return reader;
    }
}
