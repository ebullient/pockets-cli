package dev.ebullient.pockets.db;

import com.github.slugify.Slugify;

public class Mapper {
    private static Slugify slugify;

    private static Slugify slugifier() {
        Slugify s = slugify;
        if (s == null) {
            s = slugify = new Slugify()
                    .withCustomReplacement("'", "")
                    .withLowerCase(true);
        }
        return s;
    }

    public static String slugify(String text) {
        return slugifier().slugify(text);
    }
}
