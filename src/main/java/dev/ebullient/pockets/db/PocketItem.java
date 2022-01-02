package dev.ebullient.pockets.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import dev.ebullient.pockets.CommonIO;
import dev.ebullient.pockets.Constants;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * An item that exists in a pocket. This is shorthand. It might match
 * an item from the compendium, or it might be a custom item with other
 * attributes.
 */
@Entity(name = Constants.ITEM_ENTITY)
@Table(name = Constants.ITEM_TABLE)
public class PocketItem extends PanacheEntity {

    public String name;
    public String slug;
    public int quantity;
    public Double weight; // weight in lbs
    public Double value; // value in gp

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Constants.POCKET_ID, nullable = false)
    Pocket pocket;

    /** Help maintain the bi-directional relationship between objects */
    public void addToPocket(Pocket pocket) {
        pocket.addItem(this);
    }

    /** Help maintain the bi-directional relationship between objects */
    public void removeFromPocket(Pocket pocket) {
        pocket.removeItem(this);
    }

    public boolean belongsTo(Pocket pocket) {
        return this.pocket.equals(pocket);
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
        return "PocketItem [name=" + name + ", quantity=" + quantity + ", slug=" + slug
                + ", value=" + value + ", weight=" + weight + "]";
    }

    public static List<PocketItem> findByName(Pocket owner, String name) {
        final String query = CommonIO.slugify(name);
        if (owner.items == null || owner.items.isEmpty()) {
            return Collections.emptyList();
        }
        return owner.items.stream()
                .filter(p -> p.slug.startsWith(query) || p.slug.matches(query))
                .collect(Collectors.toList());
    }

    public static Map<String, Integer> fieldWidths() {
        Map<String, Integer> fieldLengths = new HashMap<>();
        Query q = PocketItem.getEntityManager().createQuery(
                "select max(p.id), max(p.quantity), max(p.weight), min(p.weight), max(p.value) from PocketItem as p");
        Object[] o = (Object[]) q.getSingleResult();
        List<Integer> len = Stream.of(o)
                .map(x -> x == null ? "" : x.toString())
                .map(x -> x.length() + 1)
                .collect(Collectors.toList());

        fieldLengths.put("id", Math.max(4, len.get(0)));
        fieldLengths.put("quantity", Math.max(3, len.get(1)));
        fieldLengths.put("weight", Math.max(4, Math.max(len.get(2), len.get(3))));
        fieldLengths.put("value", Math.max(4, len.get(4)));
        return fieldLengths;
    }
}
