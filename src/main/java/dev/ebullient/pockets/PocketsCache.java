package dev.ebullient.pockets;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectWriter;

import dev.ebullient.pockets.db.Pocket;
import dev.ebullient.pockets.db.PocketItem;
import dev.ebullient.pockets.reference.PocketReference;

class PocketsCache {

    private Map<String, Integer> fieldWidths;
    private Map<String, String> ansiFormat;
    private Map<String, String> pocketEmoji;

    @JsonIgnore
    private File cacheFile;

    public void updateFieldWidths(Pocket p) {
        if (fieldWidths == null) {
            checkFieldWidths();
        } else if (Pocket.updateCachedWidth(p, fieldWidths)) {
            CommonIO.pocketTableFormat(fieldWidths, ansiFormat);
        }
    }

    public void updateFieldWidths(PocketItem pi) {
        if (fieldWidths == null) {
            checkFieldWidths();
        } else if (PocketItem.updateCachedWidth(pi, fieldWidths)) {
            CommonIO.pocketItemTableFormat(fieldWidths, ansiFormat);
        }
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
        return pocketEmoji.computeIfAbsent(pocketRef, k -> PocketReference.emojiForSlug(k));
    }

    public void persist() {
        final ObjectWriter writer = Constants.MAPPER.writer(new DefaultPrettyPrinter());
        try {
            writer.writeValue(cacheFile, this);
        } catch (IOException e) {
            if (Term.isDebug()) {
                e.printStackTrace(Term.err());
            }
        }
    }

    public static class Builder {
        File cacheFile;
        boolean clearCache;

        public Builder() {
        }

        public Builder setConfigDirectory(File configDirectory) {
            this.cacheFile = new File(configDirectory, "cache.json");
            return this;
        }

        public Builder setClearCache(boolean clearCache) {
            this.clearCache = clearCache;
            return this;
        }

        public PocketsCache build() {
            PocketsCache cache = readCacheFile();
            cache.cacheFile = cacheFile;
            return cache;
        }

        private PocketsCache readCacheFile() {
            if (cacheFile.exists() && !clearCache) {
                try {
                    return Constants.MAPPER.readValue(cacheFile, PocketsCache.class);
                } catch (IOException e) {
                    if (Term.isDebug()) {
                        e.printStackTrace(Term.err());
                    }
                }
            }
            return new PocketsCache();
        }

    }
}
