package dev.ebullient.pockets.db;

import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "PocketItem")
@JsonIdentityInfo(scope = PocketItem.class, generator = PropertyGenerator.class, property = "id")
public class PocketItem extends PanacheEntity {

    @ManyToOne
    @JsonIdentityReference(alwaysAsId = true)
    public Pocket pocket;

    @ManyToOne
    @JsonIdentityReference(alwaysAsId = true)
    public Item item;

    public String slug;

    public long quantity;
    public double cumulativeValue;
    public double cumulativeWeight;
    public int cumulativeBulk;

    public PocketItem() {
    }

    public PocketItem(Pocket p, String slug) {
        this.pocket = p;
        this.slug = slug;
    }

    public long add(Long amount) {
        if (pocket.isBottomless()) {
            return amount;
        }
        quantity += Objects.requireNonNullElse(amount, 1L);
        calculateUnitValue();
        calculateWeight();
        calculateBulk();
        return quantity;
    }

    public long remove(Long amount) {
        if (pocket.isBottomless()) {
            return amount;
        }
        if (amount == null || amount == quantity) { // remove ALL ..
            // removed all of the items from the pocket
            // TODO: configuration setting to completely remove vs. hold at 0
            amount = quantity;
            quantity = 0;
        } else if (amount > quantity) {
            throw new InvalidPocketState(true,
                    "Cannot remove %s %s: Pocket %s only contains %s.",
                    amount, item.name, pocket.name, quantity);
        } else {
            quantity -= amount;
            calculateUnitValue();
            calculateWeight();
            calculateBulk();
        }
        return amount;
    }

    void calculateUnitValue() {
        if (item.itemDetails.baseUnitValue != null) {
            cumulativeValue = quantity * item.itemDetails.baseUnitValue;
        }
    }

    void calculateWeight() {
        if (item.itemDetails.weight != null) {
            cumulativeWeight = quantity * item.itemDetails.weight;
        }
    }

    void calculateBulk() {
        if (item.itemDetails.bulk != null) {
            switch (item.itemDetails.bulk) {
                case "-":
                case "negligible":
                    cumulativeBulk = (int) (quantity / 1000);
                    break;
                case "L":
                case "light":
                    cumulativeBulk = (int) (quantity / 10);
                    break;
                default:
                    cumulativeBulk = (int) (Integer.parseInt(item.itemDetails.bulk) * quantity);
            }
        }
    }

    @SuppressWarnings("unused")
    public String currentValue() { // templates
        if (pocket.isBottomless()) {
            return "N/A";
        }
        ProfileConfigData pcd = pocket.profile.config;
        return pcd.valueToString(cumulativeValue);
    }

    @SuppressWarnings("unused")
    public String bulkWeight() { // templates
        if (pocket.isBottomless()) {
            return "N/A";
        }
        ProfileConfigData pcd = pocket.profile.config;
        if (pcd.preset == PresetFlavor.pf2e) {
            return bulkToString();
        }
        return pcd.weightToString(cumulativeWeight);
    }

    private String bulkToString() {
        ProfileConfigData pcd = pocket.profile.config;
        if (item.itemDetails.bulk == null) {
            return "";
        }
        return pcd.bulkToString(cumulativeBulk == 0
                ? item.itemDetails.bulk
                : cumulativeBulk + "");
    }
}
