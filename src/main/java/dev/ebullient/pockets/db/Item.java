package dev.ebullient.pockets.db;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

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

    public int quantity;
    public Double weight; // weight in lbs
    public Double gpValue; // value in gp
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
                .filter(p -> p.slug.startsWith(query))
                .collect(Collectors.toList());
    }
}
