package dev.ebullient.pockets.reference;

import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.pockets.CommonIO;
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
        }
        return ref;
    }
}
