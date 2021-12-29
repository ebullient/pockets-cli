package dev.ebullient.pockets.db;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
    public String description;
    public int quantity;
    public Double weight; // weight in lbs
    public Double value; // value in gp

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = Constants.POCKET_ID, nullable = false)
    Pocket pocket;

    /** Help maintain the bi-directional relationship between objects */
    public void addToPocket(Pocket pocket) {
        this.pocket = pocket;
        pocket.addItem(this);
    }
}
