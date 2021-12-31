package dev.ebullient.pockets.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.Transient;

public enum PocketType {
    Pouch("👛", "pouch", null),
    Backpack("🎒", "backpack", null),
    Basket("🧺", "basket", null),
    Haversack("👝", "Handy Haversack", "haversack"),
    BagOfHolding("🧳", "Bag of Holding", "bag-of-holding"),
    PortableHole("🕳 ", "Portable Hole", "portable-hole"),
    Sack("🗑", "sack", null),
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

    public Pocket createPocket(Optional<String> name) {
        switch (this) {
            default:
            case Backpack:
                return createBackpack(name.orElse("Backpack"));
            case Basket:
                return createBasket(name.orElse("Basket"));
            case Pouch:
                return createPouch(name.orElse("Pouch"));
            case Haversack:
                return createHaversack(name.orElse(prettyName));
            case BagOfHolding:
                return createBagOfHolding(name.orElse(prettyName));
            case PortableHole:
                return createPortableHole(name.orElse(prettyName));
            case Sack:
                return createSack(name.orElse("Sack"));
            case Custom:
                return createCustom(name.orElse("Pocket"));
        }
    }

    /** Custom */
    public static Pocket createCustom(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Custom;
        pocket.name = name;
        pocket.magic = false;
        return pocket;
    }

    public static Pocket createBackpack(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Backpack;
        pocket.name = name;
        pocket.max_volume = 1;
        pocket.max_weight = 30;
        pocket.weight = 5;
        pocket.magic = false;
        return pocket;
    }

    public static Pocket createBasket(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Basket;
        pocket.name = name;
        pocket.max_volume = 2;
        pocket.max_weight = 40;
        pocket.weight = 2;
        pocket.magic = false;
        return pocket;
    }

    public static Pocket createPouch(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Pouch;
        pocket.name = name;
        pocket.max_volume = 0.2;
        pocket.max_weight = 6;
        pocket.weight = 1;
        pocket.magic = false;
        return pocket;
    }

    /** Haversacks are magical. One: up to you to manage volume between center & side pockets */
    public static Pocket createHaversack(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Haversack;
        pocket.name = name;
        pocket.max_volume = 12;
        pocket.max_weight = 120;
        pocket.weight = 5;
        pocket.magic = true;
        return pocket;
    }

    public static Pocket createBagOfHolding(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.BagOfHolding;
        pocket.name = name;
        pocket.max_volume = 64;
        pocket.max_weight = 500;
        pocket.weight = 15;
        pocket.magic = true;
        return pocket;
    }

    public static Pocket createPortableHole(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.PortableHole;
        pocket.name = name;
        pocket.max_volume = 282.7;
        pocket.max_weight = 0; // the limit is volume, not weight
        pocket.weight = 0;
        pocket.magic = true;
        return pocket;
    }

    public static Pocket createSack(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Sack;
        pocket.name = name;
        pocket.max_volume = 1;
        pocket.max_weight = 30;
        pocket.weight = 0.5;
        pocket.magic = false;
        return pocket;
    }

    public static class PocketCandidates extends ArrayList<String> {
        PocketCandidates() {
            super(PocketType.getVariants());
        }
    }

    public static Collection<? extends String> getVariants() {
        List<String> candidates = new ArrayList<>();
        for (PocketType type : PocketType.values()) {
            candidates.add(type.slug);
        }
        return candidates;
    }
}
