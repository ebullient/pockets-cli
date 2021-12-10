package dev.ebullient.pockets.db;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * A pocket (or backpack, or haversack, or purse, or ... )
 */
@Entity
public class Pocket extends PanacheEntity {
    public String name;
    public double max_volume; // in cubic ft
    public double max_capacity; // in lbs
    public double weight; // weight of the pocket itself
    public boolean magic; // magic pockets always weigh the same
    public PocketType type;

    /** Many items in this pocket */
    @OneToMany(mappedBy = "pocket", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<PocketItem> items;

    /** Add an item to the pocket: establish bi-directional relationship */
    public void addPocketItem(PocketItem item) {
        items.add(item);
        item.setPocket(this);
    }

    /** Remove an item from the pocket: clear bi-directional relationship */
    public void removeItem(PocketItem item) {
        items.remove(item);
        item.setPocket(null);
    }

    public enum PocketType {
        Pouch,
        Backpack,
        Haversack,
        BagOfHolding,
        PortableHole
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
        pocket.max_volume = 1 / 5; // 1/5 cubic foot is weirdly precise
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
}
