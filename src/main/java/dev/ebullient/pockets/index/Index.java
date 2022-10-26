package dev.ebullient.pockets.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.pockets.db.Mapper;
import dev.ebullient.pockets.io.PocketTui;

public class Index {

    private static final IndexData BUILT_IN = initBuiltInData();

    private static IndexData initBuiltInData() {
        try (InputStream is = Index.class.getResourceAsStream("/index.json")) {
            final IndexData builtin = IndexConstants.FROM_JSON.readValue(is, IndexData.class);
            builtin.pockets.forEach((k, v) -> v.idSlug = k);
            builtin.items.forEach((k, v) -> v.idSlug = k);
            return builtin;
        } catch (IOException e) {
            throw new RuntimeException("Error reading index.json");
        }
    }

    static class IndexData {
        final Map<String, ItemReference> items = new TreeMap<>();
        final Map<String, PocketReference> pockets = new TreeMap<>();

        boolean isEmpty() {
            return items.isEmpty() && pockets.isEmpty();
        }

        ItemReference getItem(String itemRef) {
            if (itemRef == null || itemRef.isEmpty()) {
                return null;
            }
            return items.get(itemRef);
        }

        List<ItemReference> findItemReference(String idSlug) {
            if (idSlug == null || idSlug.isEmpty()) {
                return List.of();
            }
            ItemReference exact = getItem(idSlug);
            if (exact != null) {
                return List.of(exact);
            }
            return items.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(idSlug))
                    .map(e -> e.getValue())
                    .collect(Collectors.toList());
        }

        PocketReference getPocket(String pocketRef) {
            if (pocketRef == null || pocketRef.isEmpty()) {
                return null;
            }
            return pockets.get(pocketRef);
        }

        List<PocketReference> findPocketReference(String idSlug) {
            if (idSlug == null || idSlug.isEmpty()) {
                return List.of();
            }
            return pockets.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(idSlug))
                    .map(e -> e.getValue())
                    .collect(Collectors.toList());
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
    public PocketReference getPocketReference(String idSlug) {
        PocketReference ref = data.getPocket(idSlug);
        if (ref == null) {
            ref = new PocketReference();
            ref.name = "Pocket (custom)";
            ref.idSlug = idSlug == null ? Mapper.slugify(ref.name) : idSlug;
            ref.custom = true;
            tui.debugf("Using custom Pocket reference for %s", idSlug);
        }
        return ref;
    }

    public List<PocketReference> findPocketReference(String nameOrId) {
        final String query = Mapper.slugify(nameOrId);
        return data.findPocketReference(query);
    }

    @JsonIgnore
    public Optional<ItemReference> getItemReference(String idSlug) {
        return Optional.ofNullable(data.getItem(idSlug));
    }

    public List<ItemReference> findItemReference(String nameOrId) {
        final String query = Mapper.slugify(nameOrId);
        return data.findItemReference(query);
    }

    public void listPocketTypes() {
        tui.outPrintln("\n🛍 Pocket Types\n");
        tui.outPrintln(tui.format().table("pocket-type", "Pocket Type", data.pockets.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().name, (o1, o2) -> o1, TreeMap::new))));
    }

    public void listItemTypes() {
        tui.outPrintln("\n🏷 Item Types\n");
        tui.outPrintln(tui.format().table("item-type", "Item Type", data.items.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().name, (o1, o2) -> o1, TreeMap::new))));
    }

    private void readIndex() {
        this.data.pockets.putAll(BUILT_IN.pockets);
        this.data.items.putAll(BUILT_IN.items);
        tui.debugf("Index: %d pockets and %d items", this.data.pockets.size(), this.data.items.size());
        data.pockets.keySet().forEach(k -> {
            if (data.items.containsKey(k)) {
                tui.warnf("@|fg(red) %s|@ refers to both a pocket and an item. The pocket will be preferred.%n",
                        k);
            }
        });
    }
}
