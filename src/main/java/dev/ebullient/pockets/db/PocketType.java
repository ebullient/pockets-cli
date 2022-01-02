package dev.ebullient.pockets.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.Transient;

import dev.ebullient.pockets.CommonIO;
import dev.ebullient.pockets.Term;

public enum PocketType {
    Backpack("🎒", "backpack", null),
    BagOfHolding("🧳", "Bag of Holding", null),
    Basket("🧺", "basket", null),
    Chest("🧰", "chest", null),
    CrossbowBoltCase("🏹", "crossbow bolt case", null),
    EfficientQuiver("🏹", "Efficient Quiver", null),
    // EfficientQuiverParts("🏹", "Efficient Quiver", "efficient-quiver-parts"),
    Haversack("👝", "Handy Haversack", "haversack"),
    // HaversackParts("👝", "Handy Haversack ", "haversack-parts"),
    MapCase("👝", "map case ", null),
    PortableHole("🕳 ", "Portable Hole", null),
    Pouch("👛", "pouch", null),
    Quiver("🏹", "quiver", null),
    Sack("🗑", "sack", null),
    ScrollCase("👝", "scroll case", null),
    Custom("🥡", "pocket", "custom");

    @Transient
    private final String icon;

    @Transient
    public final String prettyName;

    @Transient
    public final String slug;

    private PocketType(String icon, String prettyName, String slug) {
        this.icon = icon;
        this.prettyName = prettyName;
        this.slug = (slug == null ? CommonIO.slugify(prettyName) : slug);
    }

    public String icon() {
        return icon;
    }

    public Pocket createPocket(Optional<String> name) {
        Pocket pocket = new Pocket();
        pocket.type = this;
        pocket.magic = false;
        pocket.name = name.orElse(titlecase(prettyName));

        switch (this) {
            case Backpack:
                pocket.max_volume = 1.0;
                pocket.max_weight = 30.0;
                pocket.weight = 5;
                break;
            case BagOfHolding:
                pocket.max_volume = 64.0;
                pocket.max_weight = 500.0;
                pocket.weight = 15;
                pocket.magic = true;
                break;
            case Basket:
                pocket.max_volume = 2.0;
                pocket.max_weight = 40.0;
                pocket.weight = 2;
                break;
            case Chest:
                pocket.max_volume = 12.0;
                pocket.max_weight = 300.0;
                pocket.weight = 25;
                break;
            case CrossbowBoltCase:
                pocket.weight = 1;
                pocket.max_weight = 2.5;
                pocket.comments = "This wooden case can hold up to 20 crossbow bolts.";
                break;
            case EfficientQuiver:
                pocket.weight = 2;
                pocket.max_weight = /* shortest */ 4.5 + /* midsize */ 36 + /* longest */ 24;
                pocket.comments = "This quiver has 3 compartments.\n"
                        + "The shortest can hold up to sixty arrows, bolts, or similar objects.\n"
                        + "The midsize holds up to eighteen javelins or similar objects.\n"
                        + "The longest holds up to six long objects, such as bows, quarterstaffs, or spears.";
                pocket.magic = true;
                break;
            // case EfficientQuiverParts:
            //     pocket.weight = 2;
            //     pocket.magic = true;
            //     pocket.c
            //     break;
            case Haversack:
                pocket.max_volume = 12.0;
                pocket.max_weight = 120.0;
                pocket.weight = 5;
                pocket.magic = true;
                pocket.comments = "This backpack has a central pouch and two side pouches.\n"
                        + "Each side pouch can hold up to 20 pounds or 2 cubic feet of material.\n"
                        + "The central pouch can hold up to 8 cubic feet or 80 pounds of material.";
                break;
            // case HaversackParts:
            //     break;
            case PortableHole:
                pocket.max_volume = 282.7;
                pocket.max_weight = 0.0; // the limit is volume, not weight
                pocket.weight = 0;
                pocket.magic = true;
                pocket.comments =
                          "The portable hole has the dimensions of a hankerchief when folded.\n"
                        + "When unfolded and placed on a solid surface, it creates a hole that is \n"
                        + "6 feet in diameter and 10 feet deep.";
                break;
            case Pouch:
                pocket.max_volume = 0.2;
                pocket.max_weight = 6.0;
                pocket.weight = 1;
                break;
            case Quiver:
                pocket.weight = 1.0;
                pocket.max_weight = 2.0;
                pocket.comments = "A quiver can hold up to 20 arrows.";
                break;
            case Sack:
                pocket.max_volume = 1.0;
                pocket.max_weight = 30.0;
                pocket.weight = 0.5;
                break;
            case MapCase:
            case ScrollCase:
                pocket.weight = 1.0;
                pocket.comments =
                          "This cylindrical leather case can hold up to ten rolled-up sheets of\n"
                        + "paper or five rolled-up sheets of parchment.";
                break;
            default:
                break;
        }
        return pocket;
    }

    String titlecase(String name) {
        if (Character.isLowerCase(name.charAt(0))) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }
        return name;
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

    public static PocketType fromParameter(String value) {
        String compare = CommonIO.slugify(value);
        for (PocketType type : PocketType.values()) {
            if (type.slug.equals(compare)) {
                return type;
            }
        }
        if ("pocket".equals(compare)) {
            return PocketType.Custom;
        }
        Term.outPrintf("Did not recognize %s, creating a custom pocket", value);
        return null;
    }
}
