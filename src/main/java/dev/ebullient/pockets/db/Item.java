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
public class Item extends PanacheEntity {

    public String name;
    public String slug;
    public int quantity;
    public Double weight; // weight in lbs
    public Double gpValue; // value in gp
    public boolean tradable = true;

    Pocket pocket;

}
