package dev.ebullient.pockets;

import java.io.IOException;
import java.io.PrintWriter;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import picocli.CommandLine;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Model.CommandSpec;

public final class Term {
    private Term() {
    }

    static final boolean picocliDebugEnabled = "DEBUG".equalsIgnoreCase(System.getProperty("picocli.trace"));

    private static PrintWriter out = new PrintWriter(System.out);
    private static PrintWriter err = new PrintWriter(System.err);

    private static boolean debug;
    private static boolean verbose = true;

    private static ColorScheme colors;
    private static CommandLine commandLine;
    private static LineReader reader;
    private static boolean dumbTerminal = false;

    public static void prepareStreams(CommandSpec spec) {
        if (spec != null) {
            Term.commandLine = spec.commandLine();
            Term.out = Term.commandLine.getOut();
            Term.err = Term.commandLine.getErr();
            Term.colors = Term.commandLine.getHelp().colorScheme();
        }
    }

    private static LineReader getReader() {
        if (reader == null) {
            reader = LineReaderBuilder.builder().build();
            dumbTerminal = reader.getTerminal().getType().startsWith("dumb");
            if (!dumbTerminal) {
                Term.out = reader.getTerminal().writer();
            }
        }
        return reader;
    }

    public static void close() {
        if (reader != null) {
            Term.out = Term.commandLine.getOut();
            try {
                reader.getTerminal().close();
            } catch (IOException ioe) {
                if (debug) {
                    err.println("Error closing terminal");
                    ioe.printStackTrace(err);
                }
            }
            reader = null;
        }
    }

    public static boolean canPrompt() {
        getReader();
        return !dumbTerminal;
    }

    public static String prompt(String prompt) {
        return getReader().readLine("\n🔷 " + prompt);
    }

    public static void setDebug(boolean debug) {
        Term.debug = debug;
    }

    public static boolean isDebug() {
        return Term.debug || picocliDebugEnabled;
    }

    public static void setBrief(boolean brief) {
        Term.verbose = !brief;
    }

    public static boolean isVerbose() {
        return Term.verbose;
    }

    public static void debugf(String format, Object... params) {
        if (isDebug()) {
            debug(String.format(format, params));
        }
    }

    public static void debug(String output) {
        if (isDebug()) {
            if (colors == null) {
                Term.out.println(output);
            } else {
                Term.out.println(colors.ansi().new Text("@|faint " + output + "|@", colors));
            }
        }
    }

    public static void errorf(String format, Object... args) {
        error(null, String.format(format, args));
    }

    public static void errorf(Throwable th, String format, Object... args) {
        error(th, String.format(format, args));
    }

    public static void error(String errorMsg) {
        error(null, errorMsg);
    }

    public static void error(Throwable ex, String errorMsg) {
        if (colors == null) {
            Term.err.println(errorMsg);
        } else {
            Term.err.println(colors.ansi().text("🛑 @|fg(red) " + errorMsg + "|@"));
        }
        Term.err.flush();
        if (ex != null && isDebug()) {
            ex.printStackTrace(err);
        }
    }

    public static void outPrintf(String format, Object... args) {
        String output = String.format(format, args);
        if (colors == null) {
            Term.out.print(output);
        } else {
            Term.out.print(colors.ansi().text(output));
        }
        Term.out.flush();
    }

    public static void outPrintln(String output) {
        if (colors == null) {
            Term.out.println(output);
        } else {
            Term.out.println(colors.ansi().new Text(output, colors));
        }
        Term.out.flush();
    }

    public static PrintWriter err() {
        return err;
    }

    public static void showUsage(CommandSpec spec) {
        spec.commandLine().usage(Term.out, colors.ansi());
    }
}
