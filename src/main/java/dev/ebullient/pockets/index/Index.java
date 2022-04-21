package dev.ebullient.pockets.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.io.PocketTui;

public class Index {

    static class IndexData {
        final Map<String, ItemReference> items = new TreeMap<>();
        final Map<String, PocketReference> pockets = new TreeMap<>();

        boolean isEmpty() {
            return items.isEmpty() && pockets.isEmpty();
        }

        ItemReference getItem(String itemRef) {
            if ( itemRef == null || itemRef.isEmpty()) {
                return null;
            }
            return items.get(itemRef);
        }

        PocketReference getPocket(String pocketRef) {
            if ( pocketRef == null || pocketRef.isEmpty()) {
                return null;
            }
            return pockets.get(pocketRef);
        }
    }

    final IndexData data = new IndexData();
    final PocketTui tui;

    public Index(PocketTui tui) {
        this.tui = tui;
    }

    public void init() {
        readIndex();
    }

    @JsonIgnore
    public PocketReference getPocketReference(String pocketRef) {
        PocketReference ref = data.getPocket(pocketRef);
        if (ref == null) {
            ref = new PocketReference();
            ref.name = "Pocket (custom)";
            ref.idSlug = Mapper.slugify(pocketRef == null ? ref.name : pocketRef);
            ref.custom = true;
            tui.debugf("Using custom Pocket reference for %s", pocketRef);
        } else {
            ref.idSlug = pocketRef;
        }
        return ref;
    }

    @JsonIgnore
    public Optional<ItemReference> getItemReference(String itemRef) {
        // Look for pockets first: an item may be listed as both.
        ItemReference ref = data.getPocket(itemRef);
        if (ref == null) {
            ref = data.getItem(itemRef);
        }

        if (ref != null) {
            ref.idSlug = itemRef;
            return Optional.of(ref);
        }
        return Optional.empty();
    }

    public void listPocketTypes() {
        tui.outPrintln(tui.format().table(data.pockets.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().name, (o1, o2) -> o1, TreeMap::new))));
    }

    public void listItemTypes() {
        tui.outPrintln(tui.format().table(data.items.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().name, (o1, o2) -> o1, TreeMap::new))));
    }

    private void readIndex() {
        try {
            InputStream is = this.getClass().getResourceAsStream("/index.json");
            IndexData builtin = IndexConstants.FROM_JSON.readValue(is, IndexData.class);
            this.data.pockets.putAll(builtin.pockets);
            this.data.items.putAll(builtin.items);
            tui.debugf("Index: %d pockets and %d items", this.data.pockets.size(), this.data.items.size());
        } catch (IOException e) {
            tui.error(e, "Error reading index.json");
        }

        data.pockets.keySet().forEach(k -> {
            if (data.items.containsKey(k)) {
                tui.warnf("@|fg(red) %s|@ refers to both a pocket and an item. The pocket will be preferred.%n",
                        k);
            }
        });
    }
}
