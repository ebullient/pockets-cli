package dev.ebullient.pockets.db;

import java.util.List;
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
    public double max_weight; // in lbs

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

    /**
     * Add an item to the pocket
     *
     * @see PocketItem#addToPocket(Pocket)
     */
    public void addItem(PocketItem item) {
        if (items.add(item)) {
            item.pocket = this;
        }
    }

    /**
     * Remove an item from the pocket
     *
     * @see PocketItem#removeFromPocket(Pocket)
     */
    public void removeItem(PocketItem item) {
        if (items.remove(item)) {
            item.pocket = null;
        }
    }

    // /** This pocket can contain several other pockets */
    // @OneToMany
    // @JoinTable(name = "NESTED_POCKETS",
    //     joinColumns = {@JoinColumn(name = "PARENT", referencedColumnName = "id")},
    //     inverseJoinColumns = {@JoinColumn(name = "CHILD", referencedColumnName = "id")})
    // public Set<Pocket> children;

    // /** This pocket can be in at most one other pocket */
    // @ManyToOne(fetch = FetchType.LAZY, optional = true)
    // @JoinTable(name = "NESTED_POCKETS",
    //     inverseJoinColumns = {@JoinColumn(name = "PARENT", referencedColumnName = "id", insertable = false, updatable = false)},
    //     joinColumns = {@JoinColumn(name = "CHILD", referencedColumnName = "id", insertable = false, updatable = false)})
    // Pocket parent;

    // /** Add a child pocket: establish bi-directional relationship */
    // public void addPocketToPocket(Pocket child) {
    //     if ( !child.contains(this) && !this.contains(child)) {
    //         children.add(child);
    //         child.parent = this;
    //     }
    // }

    // /** Remove an child pocket: clear bi-directional relationship */
    // public void removePocketFromPocket(Pocket child) {
    //     if ( children.remove(child) ) {
    //         child.parent = null;
    //     }
    // }

    // /** TODO: This is a thought exercise and therefore suspect. Test me */
    // public boolean contains(Pocket pocket) {
    //     if ( children == null || children.isEmpty() )
    //         return false;
    //     boolean contains = false;
    //     for ( Pocket p : children ) {
    //         if ( p.equals(pocket) || p.contains(pocket)) {
    //             contains = true;
    //             break;
    //         }
    //     }
    //     return contains;
    // }

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
        return "Pocket [items=" + items + ", magic=" + magic + ", max_volume=" + max_volume + ", max_weight="
                + max_weight + ", name=" + name + ", slug=" + slug + ", type=" + type + ", weight=" + weight + "]";
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
}
