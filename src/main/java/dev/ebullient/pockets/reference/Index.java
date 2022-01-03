package dev.ebullient.pockets.reference;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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

    public Index merge(Index userItems) {
        items.putAll(userItems.items);
        pockets.putAll(userItems.pockets);
        return this;
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

    public static class Builder {
        File userIndexFile;

        public Builder() {
        }

        public Builder setConfigDirectory(File configDirectory) {
            this.userIndexFile = new File(configDirectory, "index.json");
            ;
            return this;
        }

        public Index build() {
            Index index = readIndex();
            Index userIndex = readUserIndex();
            return index.merge(userIndex);
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
