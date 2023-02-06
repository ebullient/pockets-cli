package dev.ebullient.pockets.io;

import dev.ebullient.pockets.Transform;
import picocli.CommandLine.ExitCode;

public class InvalidPocketState extends RuntimeException {
    boolean hasCause = false;
    int exitCode = ExitCode.SOFTWARE;

    public InvalidPocketState(int exitCode) {
        super("");
        this.exitCode = exitCode;
    }

    public InvalidPocketState(String format, Object... params) {
        super(String.format(format, params));
    }

    public InvalidPocketState(boolean userError, String format, Object... params) {
        super(String.format(format, params));
        this.exitCode = userError ? ExitCode.USAGE : ExitCode.SOFTWARE;
    }

    public InvalidPocketState(int exitCode, String format, Object... params) {
        super(String.format(format, params));
        this.exitCode = exitCode;
    }

    public InvalidPocketState(Throwable cause, String format, Object... params) {
        super(String.format(format, params), cause);
        hasCause = true;
    }

    public InvalidPocketState(Throwable cause, int exitCode, String format, Object... params) {
        super(String.format(format, params), cause);
        this.exitCode = exitCode;
        hasCause = true;
    }

    Throwable ipsOrCause() {
        if (hasCause) {
            return getCause();
        }
        return this;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (hasCause) {
            return null;
        }
        return super.fillInStackTrace();
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public boolean showMessage() {
        return hasCause || !Transform.isBlank(getMessage());
    }
}
