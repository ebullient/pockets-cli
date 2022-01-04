package dev.ebullient.pockets.reference;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.pockets.CommonIO;
import dev.ebullient.pockets.Constants;
import dev.ebullient.pockets.Term;

public class Index {
    Map<String, ItemReference> items = new TreeMap<>();
    Map<String, PocketReference> pockets = new TreeMap<>();

    boolean isEmpty() {
        return items.isEmpty() && pockets.isEmpty();
    }

    @JsonIgnore
    public PocketReference getPocketReference(String pocketRef) {
        PocketReference ref = pockets.get(pocketRef);
        if (ref == null) {
            ref = new PocketReference();
            ref.name = "Pocket (custom)";
            ref.idSlug = CommonIO.slugify(pocketRef == null ? ref.name : pocketRef);
            ref.custom = true;
            Term.debugf("Using custom Pocket reference for %s", pocketRef);
        } else {
            ref.idSlug = pocketRef;
        }
        return ref;
    }

    @JsonIgnore
    public Optional<ItemReference> getItemReference(String itemRef) {
        // Look for pockets first
        ItemReference ref = pockets.get(itemRef);
        if (ref == null) {
            ref = items.get(itemRef);
        }

        if (ref != null) {
            ref.idSlug = itemRef;
            return Optional.of(ref);
        }
        return Optional.empty();
    }

    public static class Builder {
        File userIndexFile;

        public Builder() {
        }

        public Builder setConfigDirectory(File configDirectory) {
            this.userIndexFile = new File(configDirectory, "index.json");
            return this;
        }

        public Index build() {
            Index index = readIndex();
            Index userIndex = readUserIndex();

            index.items.putAll(userIndex.items);
            index.pockets.putAll(userIndex.pockets);

            index.pockets.keySet().forEach(k -> {
                if (index.items.containsKey(k)) {
                    Term.outPrintf("🔶 @|fg(red) %s|@ refers to both a pocket and an item. The pocket will be preferred.%n",
                            k);
                }
            });
            return index;
        }

        private Index readIndex() {
            try {
                return Constants.MAPPER.readValue(ClassLoader.getSystemResourceAsStream("index.json"), Index.class);
            } catch (IOException e) {
                if (Term.isDebug()) {
                    e.printStackTrace(Term.err());
                }
            }
            return new Index();
        }

        private Index readUserIndex() {
            if (userIndexFile != null && userIndexFile.exists()) {
                try {
                    return Constants.MAPPER.readValue(userIndexFile, Index.class);
                } catch (IOException e) {
                    if (Term.isDebug()) {
                        e.printStackTrace(Term.err());
                    }
                }
            }
            return new Index();
        }
    }
}
