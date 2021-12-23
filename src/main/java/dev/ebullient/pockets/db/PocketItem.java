package dev.ebullient.pockets.db;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * An item that exists in a pocket. This is shorthand. It might match
 * an item from the compendium, or it might be a custom item with other
 * attributes.
 */
@Entity
public class PocketItem extends PanacheEntity {
    public String description;
    public int quantity;
    public double weight; // weight in lbs
    public double value;  // value in gp

    @ManyToOne(fetch = FetchType.LAZY)
    Pocket pocket;

    /** Help maintain the bi-directional relationship between objects */
    public void setPocket(Pocket pocket) {
        this.pocket = pocket;
    }
}
