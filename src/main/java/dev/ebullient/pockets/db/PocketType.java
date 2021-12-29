package dev.ebullient.pockets.db;

import javax.persistence.Transient;

public enum PocketType {
    Pouch("👛", "pouch", null),
    Backpack("🎒", "backpack", null),
    Haversack("👝", "Handy Haversack", "haversack"),
    BagOfHolding("👜", "Bag of Holding", "bag-of-holding"),
    PortableHole("🕳 ", "Portable Hole", "portable-hole"),
    Sack("🧺", "sack", null),
    Custom("🧰", "pocket", null);

    @Transient
    private final String icon;

    @Transient
    public final String prettyName;

    @Transient
    public final String slug;

    private PocketType(String icon, String prettyName, String slug) {
        this.icon = icon;
        this.prettyName = prettyName;
        this.slug = (slug == null ? prettyName : slug);
    }

    public String icon() {
        return icon;
    }
}
