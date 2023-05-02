package dev.ebullient.pockets.db;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * An item that exists in a pocket.
 */
@Entity(name = EntityConstants.ITEM_ENTITY)
@Table(name = EntityConstants.ITEM_TABLE)
public class Item extends PanacheEntity {

    @Size(min = 1, max = 50)
    public String name;

    @NotNull // identifier: name-as-slug
    public String slug;

    public String itemRef;

    public int quantity;
    public Double weight; // weight in lbs
    public Double cpValue; // value in cp
    public boolean tradable = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = EntityConstants.ITEM_POCKET_ID, nullable = false)
    Pocket pocket;

    /** Maintain the bi-directional relationship between objects */
    public boolean addToPocket(Pocket pocket) {
        return pocket.addItem(this);
    }

    /** Maintain the bi-directional relationship between objects */
    public boolean removeFromPocket(Pocket pocket) {
        return pocket.removeItem(this);
    }

    public boolean belongsTo(Pocket pocket) {
        return pocket.equals(this.pocket);
    }

    @Override
    public void persist() {
        slug = Mapper.slugify(name);
        super.persist();
    }

    @Override
    public void persistAndFlush() {
        slug = Mapper.slugify(name);
        super.persistAndFlush();
    }

    public static List<Item> findByName(Pocket owner, String name) {
        final String query = Mapper.slugify(name);

        if (owner.items == null || owner.items.isEmpty()) {
            return Collections.emptyList();
        }
        return owner.items.stream()
                .filter(i -> i.slug.startsWith(query))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Item [cpValue=" + cpValue + ", name=" + name + ", pocket=" + pocket + ", quantity=" + quantity
                + ", slug=" + slug + ", tradable=" + tradable + ", weight=" + weight + "]";
    }
}
