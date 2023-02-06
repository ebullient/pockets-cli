package dev.ebullient.pockets;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.ebullient.pockets.config.Types.CurrencyRef;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Journal;
import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketCurrency;
import dev.ebullient.pockets.db.PocketItem;
import dev.ebullient.pockets.db.Posting;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;
import dev.ebullient.pockets.db.Profile;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.test.junit.main.LaunchResult;

public class Util {
    public final static Path PROJECT_PATH = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
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

    public static Function<Stream<String>, String> replaceFunction = stream -> stream
            // Remove debug/info messages
            .filter(x -> !LOG_PATTERN.matcher(x).matches())
            .filter(x -> !x.startsWith("[INFO]"))
            .filter(s -> !s.startsWith("🔧"))
            .filter(s -> !s.startsWith("&allowNullBody")) // debug from a camel route..
            // replace many whitespaces with a single whitespace
            .map(s -> s.replaceAll(" +", "✦"))
            // replace id strings with placeholders
            .map(s -> s.replaceAll("\\[[\\d ]+]", "[id]"))
            .collect(Collectors.joining("\n"));

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

    public static void checkItem(Item created, Item i) {
        assertThat(created.slug).isEqualTo(i.slug);
        assertThat(created.name).isEqualTo(i.name);
        assertThat(created.itemDetails).isEqualTo(i.itemDetails);
    }

    public static void checkPocket(Pocket created, Pocket p) {
        assertThat(created.slug).isEqualTo(p.slug);
        assertThat(created.name).isEqualTo(p.name);
        assertThat(created.emoji).isEqualTo(p.emoji);
        assertThat(created.pocketDetails).isEqualTo(p.pocketDetails);
    }

    public static PocketItem checkPocketItemLinks(Pocket p, Item i, long quantity) {
        return checkPocketItemLinks(p, 1, i, 1, quantity);
    }

    public static PocketItem checkPocketItemLinks(Pocket p, int p_size, Item i, int i_size, long quantity) {
        assertThat(p.pocketItems).hasSize(p_size);
        assertThat(i.pocketItems).hasSize(i_size);

        PocketItem p_i = p.pocketItems.get(i.slug);
        assertThat(p_i).isNotNull();
        assertThat(p_i.item).isEqualTo(i);
        assertThat(p_i.pocket).isEqualTo(p);
        assertThat(p_i.quantity).isEqualTo(quantity);
        return p_i;
    }

    public static void checkCreatePosting(Posting p, ItemType itemType, String itemId, Object o) {
        assertThat(p).isNotNull();
        assertThat(p.type).isEqualTo(Posting.PostingType.CREATE);
        assertThat(p.itemType).isEqualTo(itemType);
        assertThat(p.itemId).isEqualTo(itemId);
        if (itemType == ItemType.POCKET) {
            checkPocket((Pocket) p.created, (Pocket) o);
        } else if (itemType == ItemType.ITEM) {
            checkItem((Item) p.created, (Item) o);
        }
    }

    public static void checkPosting(Posting p, PostingType type, ItemType itemType, String pocketId, String itemId,
            Long quantity) {
        assertThat(p).isNotNull();
        assertThat(p.type).isEqualTo(type);
        assertThat(p.itemType).isEqualTo(itemType);
        assertThat(p.pocketId).isEqualTo(pocketId);
        assertThat(p.itemId).isEqualTo(itemId);
        assertThat(p.quantity).isEqualTo(quantity);
    }

    public static void assertProfiles(int len) {
        List<Profile> list = Profile.listAll();
        assertThat(list).describedAs("Has Profile %s", list).hasSize(len);
    }

    public static void assertJournals(int len) {
        List<Journal> list = Journal.listAll();
        assertThat(list).describedAs("Has Journal %s", list).hasSize(len);
    }

    public static void assertPockets(int len) {
        List<Pocket> list = Pocket.listAll();
        assertThat(list).describedAs("Has Pocket %s", list).hasSize(len);
    }

    public static void assertItems(int len) {
        List<Item> list = Item.listAll();
        assertThat(list).describedAs("Has Item %s", list).hasSize(len);
    }

    public static void assertPocketCurrency(int len) {
        List<PocketCurrency> list = PocketCurrency.listAll();
        assertThat(list).describedAs("Has PocketCurrency %s", list).hasSize(len);
    }

    public static void assertPocketItems(int len) {
        List<PocketItem> list = PocketItem.listAll();
        assertThat(list).describedAs("Has PocketItem %s", list).hasSize(len);
    }

    public static PocketCurrency checkPocketCurrencyLinks(Profile profile, Pocket p, int p_size, String c, long quantity) {
        assertThat(p.pocketCurrency).hasSize(p_size);
        CurrencyRef currencyRef = profile.config.currencyRef(c);

        PocketCurrency p_c = p.pocketCurrency.get(c);
        assertThat(p_c).isNotNull();
        assertThat(p_c.currency).isEqualTo(c);
        assertThat(p_c.pocket).isEqualTo(p);
        assertThat(p_c.quantity).describedAs("%s quantity", c).isEqualTo(quantity);
        assertThat(p_c.baseValue).describedAs("%s cumulative value", c).isEqualTo(quantity * currencyRef.unitConversion);
        return p_c;
    }

    public static void deleteDir(Path path) {
        if (!path.toFile().exists()) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            paths.sorted(Comparator.reverseOrder())
                    .filter(p -> !path.equals(p))
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new InvalidPocketState(e, "Unable to delete test directories: %s", e.getMessage());
        }
        assertThat(path.toFile()).exists();
    }

    public static void assertFileContent(Path targetDir, String fileName, CharSequence... values) throws IOException {
        Path filePath = targetDir.resolve(fileName);
        assertThat(filePath).exists();

        String contents = Files.readString(filePath);
        assertThat(contents).contains(values);
    }
}
