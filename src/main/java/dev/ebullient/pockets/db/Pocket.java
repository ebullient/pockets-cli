package dev.ebullient.pockets.db;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import dev.ebullient.pockets.Input;
import dev.ebullient.pockets.Log;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * A pocket (or backpack, or haversack, or purse, or ... )
 */
@Entity
@Table(name = "pocket")
public class Pocket extends PanacheEntity {

    public enum PocketType {
        Pouch("👛", "pouch"),
        Backpack("🎒", "backpack"),
        Haversack("👝", "Handy Haversack"),
        BagOfHolding("👜", "Bag of Holding"),
        PortableHole("🕳 ", "Portable Hole"),
        Sack("🧺", "sack"),
        Custom("🧰", "pocket");

        @Transient
        private final String icon;

        @Transient
        public final String prettyName;

        private PocketType(String icon, String prettyName) {
            this.icon = icon;
            this.prettyName = prettyName;
        }

        public String icon() {
            return icon;
        }
    }

    @NotBlank
    public String name;

    @NotBlank
    public String slug;

    @NotNull
    public double max_volume; // in cubic ft

    @NotNull
    public double max_capacity; // in lbs

    @NotNull
    public double weight; // weight of the pocket itself

    @NotNull
    public boolean magic; // magic pockets always weigh the same

    @NotNull
    public PocketType type;

    /** Many items in this pocket */
    @OneToMany(mappedBy = "pocket", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<PocketItem> items;

    @Override
    public void persist() {
        this.slug = Input.slugifier().slugify(name);
        super.persist();
    }

    @Override
    public void persistAndFlush() {
        this.slug = Input.slugifier().slugify(name);
        super.persistAndFlush();
    }

    /** Add an item to the pocket: establish bi-directional relationship */
    public void addItem(PocketItem item) {
        items.add(item);
    }

    /** Remove an item from the pocket: clear bi-directional relationship */
    public void removeItem(PocketItem item) {
        items.remove(item);
    }

    /**
     * Find pocket by name.
     *
     * @param name
     * @return List of name
     */
    public static List<Pocket> findByName(String name) {
        return list("slug", Input.slugifier().slugify(name));
    }

    public void describe() {
        if (magic) {
            Log.outPrintf(
                    "@|bold,underline This %s is magical.|@ It always weighs %s pounds, regardless of its contents.%n",
                    type.prettyName, weight);
        } else {
            Log.outPrintf("This %s weighs %s pounds when empty.%n", type.prettyName, weight);
        }
        if (magic && max_capacity == 0) {
            Log.outPrintf("It can hold %s cubic feet of gear.%n", max_volume);
        } else {
            Log.outPrintf("It can hold %s pounds or %s cubic feet of gear.%n", max_capacity, max_volume);
        }
        Log.outPrintln("");
    }

    public static Pocket createPocket(PocketType type, Optional<String> name) {
        switch (type) {
            default:
            case Backpack:
                return createBackpack(name.orElse("Backpack"));
            case Pouch:
                return createPouch(name.orElse("Pouch"));
            case Haversack:
                return createHaversack(name.orElse(type.prettyName));
            case BagOfHolding:
                return createBagOfHolding(name.orElse(type.prettyName));
            case PortableHole:
                return createPortableHole(name.orElse(type.prettyName));
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

    /** Pouch */
    public static Pocket createBackpack(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Backpack;
        pocket.name = name;
        pocket.max_volume = 1;
        pocket.max_capacity = 30;
        pocket.weight = 5;
        pocket.magic = false;
        return pocket;
    }

    /** Pouch */
    public static Pocket createPouch(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Pouch;
        pocket.name = name;
        pocket.max_volume = 0.2;
        pocket.max_capacity = 6;
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
        pocket.max_capacity = 120;
        pocket.weight = 5;
        pocket.magic = true;
        return pocket;
    }

    /** Bag of Holding */
    public static Pocket createBagOfHolding(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.BagOfHolding;
        pocket.name = name;
        pocket.max_volume = 64;
        pocket.max_capacity = 500;
        pocket.weight = 15;
        pocket.magic = true;
        return pocket;
    }

    /** Portable Hole */
    public static Pocket createPortableHole(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.PortableHole;
        pocket.name = name;
        pocket.max_volume = 282.7;
        pocket.max_capacity = 0; // the limit is volume, not weight
        pocket.weight = 0;
        pocket.magic = true;
        return pocket;
    }

    /** Sack */
    public static Pocket createSack(String name) {
        Pocket pocket = new Pocket();
        pocket.type = PocketType.Sack;
        pocket.name = name;
        pocket.max_volume = 1;
        pocket.max_capacity = 30;
        pocket.weight = 0.5;
        pocket.magic = false;
        return pocket;
    }
}
