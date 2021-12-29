package dev.ebullient.pockets.db;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import dev.ebullient.pockets.CommonIO;
import dev.ebullient.pockets.Constants;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * A pocket (or backpack, or haversack, or purse, or ... )
 */
@Entity(name = Constants.POCKET_ENTITY)
@Table(name = Constants.POCKET_TABLE)
public class Pocket extends PanacheEntity {

    @Size(min = 1, max = 50)
    public String name;

    @NotNull
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
    @Convert(converter = PocketTypeConverter.class)
    public PocketType type;

    /** Many items in this pocket */
    @OneToMany(mappedBy = Constants.POCKET_TABLE, fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<PocketItem> items;

    /** Add an item to the pocket: establish bi-directional relationship */
    public void addItem(PocketItem item) {
        items.add(item);
    }

    /** Remove an item from the pocket: clear bi-directional relationship */
    public void removeItem(PocketItem item) {
        items.remove(item);
    }

    @Override
    public void persist() {
        slug = CommonIO.slugify(name);
        super.persist();
    }

    @Override
    public void persistAndFlush() {
        slug = CommonIO.slugify(name);
        super.persistAndFlush();
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "name='" + name + '\'' +
                ", max_volume=" + max_volume +
                ", max_capacity=" + max_capacity +
                ", weight=" + weight +
                ", magic=" + magic +
                ", type=" + type +
                ", items=" + items +
                ", id=" + id +
                '}';
    }

    /**
     * Find pocket by name
     *
     * @param name -- will be slugified
     * @return List of pockets that match the slugified name
     */
    public static List<Pocket> findByName(String name) {
        final String query = CommonIO.slugify(name);
        List<Pocket> allPockets = Pocket.listAll();
        return allPockets.stream()
                .filter(p -> p.slug.startsWith(query) || p.slug.matches(query))
                .collect(Collectors.toList());
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
