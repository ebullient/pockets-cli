package dev.ebullient.pockets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import dev.ebullient.pockets.reference.Index;
import dev.ebullient.pockets.reference.PocketReference;

@ApplicationScoped
public class Config {
    private File configDirectory;

    private Cache cache;
    private File cacheFile;

    private Index index;

    private final ObjectMapper mapper;

    public Config() {
        if (io.quarkus.runtime.LaunchMode.current().isDevOrTest()) {
            configDirectory = Path.of(System.getProperty("user.dir"), "target/.pockets").toFile();
        } else {
            configDirectory = Path.of(System.getProperty("user.home"), ".pockets").toFile();
        }
        Term.debug("Default config directory: " + configDirectory);

        mapper = new ObjectMapper();
        mapper.setVisibility(mapper.getVisibilityChecker().withFieldVisibility(Visibility.ANY));
    }

    void setConfigPath(File configDir) {
        this.configDirectory = configDir;
    }

    void init() {
        configDirectory.mkdirs();

        cacheFile = new File(configDirectory, "cache.json");
        cache = readCache(cacheFile);

        index = readIndex();
        File userIndexFile = new File(configDirectory, "index.json");
        readUserIndex(userIndexFile);
    }

    public void close() {
        final ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(cacheFile, cache);
        } catch (IOException e) {
            if (Term.isDebug()) {
                e.printStackTrace(Term.err());
            }
        }
    }

    private Index readIndex() {
        try {
            return mapper.readValue(ClassLoader.getSystemResourceAsStream("index.json"), Index.class);
        } catch (IOException e) {
            if (Term.isDebug()) {
                e.printStackTrace(Term.err());
            }
        }
        return new Index();
    }

    private void readUserIndex(File userIndexFile) {
        if (userIndexFile.exists()) {
            try {
                Index userItems = mapper.readValue(userIndexFile, Index.class);
                index.merge(userItems);
            } catch (IOException e) {
                if (Term.isDebug()) {
                    e.printStackTrace(Term.err());
                }
            }
        }
    }

    Cache readCache(File cacheFile) {
        if (cacheFile.exists()) {
            try {
                return mapper.readValue(cacheFile, Cache.class);
            } catch (IOException e) {
                if (Term.isDebug()) {
                    e.printStackTrace(Term.err());
                }
            }
        }
        return new Cache();
    }

    public Cache getCache() {
        return cache;
    }

    public PocketReference getPocketReference(Pocket pocket) {
        return getPocketReference(pocket.pocketRef);
    }

    public PocketReference getPocketReference(String pocketRef) {
        return index.getPocketReference(pocketRef);
    }

    static class Cache {
        private Map<String, Integer> fieldWidths;
        private Map<String, String> ansiFormat;
        private Map<String, String> pocketEmoji;

        public Map<String, Integer> getFieldWidths() {
            if (fieldWidths == null) {
                checkFieldWidths();
            }
            return fieldWidths;
        }

        public void checkFieldWidths() {
            if (fieldWidths == null) {
                fieldWidths = new HashMap<>();
            }
            if (ansiFormat == null) {
                ansiFormat = new HashMap<>();
            }
            Pocket.fieldWidths(fieldWidths); // get initial values
            CommonIO.pocketTableFormat(fieldWidths, ansiFormat);

            PocketItem.fieldWidths(fieldWidths); // get initial values
            CommonIO.pocketItemTableFormat(fieldWidths, ansiFormat);
        }

        public String ansiFormat(String key) {
            if (ansiFormat == null) {
                checkFieldWidths();
            }
            return ansiFormat.get(key);
        }

        public void setPocketEmoji(String pocketRef, String emoji) {
            if (pocketEmoji == null) {
                pocketEmoji = new HashMap<>();
            }
            pocketEmoji.put(pocketRef, emoji);
        }

        public String getPocketEmoji(String pocketRef) {
            if (pocketEmoji == null) {
                pocketEmoji = new HashMap<>();
            }
            return pocketEmoji.computeIfAbsent(pocketRef, k -> emojiForSlug(k));
        }
    }

    static String emojiForSlug(String idSlug) {
        switch (idSlug) {
            case "backpack":
                return "🎒";
            case "bag-of-holding":
                return "🧳";
            case "basket":
                return "🧺";
            case "chest":
                return "🧰";
            case "portable-hole":
                return "🕳 ";
            case "pouch":
                return "👛";
            case "sack":
                return "🗑";
            case "crossbow-bolt-case":
                return "🏹";
            default:
                if (idSlug.endsWith("quiver")) {
                    return "🏹";
                }
                if (idSlug.contains("haversack")) {
                    return "👝";
                }
                if (idSlug.endsWith("-case")) {
                    return "🗞";
                }
                return "🥡";
        }
    }
}
