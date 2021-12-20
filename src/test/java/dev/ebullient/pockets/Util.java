package dev.ebullient.pockets;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.quarkus.test.junit.main.LaunchResult;

public class Util {

    public static String outputWithoutLogs(LaunchResult result) {
        return result.getOutputStream().stream()
                .filter(x -> !Util.LOG_PATTERN.matcher(x).matches()).collect(Collectors.joining("\n"));
    }

    public static final Pattern LOG_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}.*");

}
