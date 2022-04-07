package dev.ebullient.pockets.db;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

/**
 * A pocket (or backpack, or haversack, or purse, or ... )
 */
public class Pocket {

    @Size(min = 1, max = 50)
    public String name;

    @NotNull // identifier: name-as-slug
    public String slug;

    @NotNull
    public String pocketRef;

    public Double max_weight; // in lbs
    public Double max_volume; // in cubic ft, might be null

    @NotNull
    public Double weight; // weight of the pocket itself

    @NotNull // extradimensional always have the same carry weight
    public boolean extradimensional = false;
}
