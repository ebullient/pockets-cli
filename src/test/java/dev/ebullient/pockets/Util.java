package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.test.junit.main.LaunchResult;

public class Util {

    public static final Pattern LOG_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}.*");

    public static String conciseOutput(LaunchResult result) {
        return replaceFunction.apply(result.getOutputStream().stream());
    }

    public static String conciseOutput(String input) {
        String[] split = input.split("\n");
        return replaceFunction.apply(Stream.of(split));
    }

    public static String conciseOutput(CharSequence input) {
        String[] split = input.toString().split("\n");
        return replaceFunction.apply(Stream.of(split));
    }

    public static String conciseOutput(List<String> input) {
        return replaceFunction.apply(input.stream());
    }

    public static void assertConciseContentContains(List<String> actual, CharSequence... values) {
        assertThat(conciseOutput(actual)).contains(Stream.of(values)
                .map(Util::conciseOutput)
                .collect(Collectors.toList()));
    }

    public static void assertConciseContentDoesNotContain(List<String> actual, CharSequence... values) {
        assertThat(conciseOutput(actual)).doesNotContain(Stream.of(values)
                .map(Util::conciseOutput)
                .collect(Collectors.toList()));
    }

    public static Function<Stream<String>, String> replaceFunction = stream -> stream
            // Remove debug/info messages
            .filter(x -> !LOG_PATTERN.matcher(x).matches())
            .filter(x -> !x.startsWith("[INFO]"))
            .filter(s -> !s.startsWith("🔧"))
            // replace id strings with placeholders
            .map(s -> s.replaceAll("\\[[\\d ]+]", "[id]"))
            // replace many whitespaces with a single whitespace
            .map(s -> s.replaceAll(" +", "✦"))
            .collect(Collectors.joining("\n"));
}
