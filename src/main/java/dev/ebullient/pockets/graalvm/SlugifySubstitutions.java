package dev.ebullient.pockets.graalvm;

import com.github.slugify.Slugify;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(Slugify.class)
public final class SlugifySubstitutions {

    @Substitute
    private String transliterate(String input) {
        throw new IllegalArgumentException("Transliteration capabilities have been disabled, as dependency is missing");
    }

}
