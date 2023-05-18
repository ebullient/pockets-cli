package dev.ebullient.pockets.db;

import java.util.Objects;
import java.util.regex.Pattern;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.CurrencyRef;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity(name = "PocketCurrency")
@JsonIgnoreProperties({ "id" })
@JsonIdentityInfo(scope = PocketCurrency.class, generator = PropertyGenerator.class, property = "currency")
public class PocketCurrency extends PanacheEntity {
    public static final Pattern COIN_QUANTITY = Pattern.compile("([\\d*]+)(.+)");

    @ManyToOne
    @NaturalId
    @JsonIdentityReference(alwaysAsId = true)
    public Pocket pocket;

    @NaturalId
    public String currency;

    public long quantity;
    public double baseValue; // quantity * unitConversion

    public String name;
    public double unitConversion;

    public PocketCurrency(Pocket pocket, String cid, CurrencyRef currencyRef) {
        this.pocket = pocket;
        this.currency = cid;
        this.name = currencyRef.name;
        this.unitConversion = currencyRef.unitConversion;
    }

    public PocketCurrency() {
    }

    public String getName() {
        if (name == null && pocket.profile != null) { // this may be null in some circumstances
            CurrencyRef ref = pocket.profile.config.currencyRef(this.currency);
            name = ref.name;
        }
        return name;
    }

    public void deposit(Long amount) {
        if (pocket.isBottomless()) {
            return;
        }
        quantity += Objects.requireNonNullElse(amount, 1L);
        calculateBaseValue();
    }

    /**
     * @param amount to withdraw, or null to withdraw everything
     * @return Amount withdrawn
     */
    public long withdraw(Long amount) {
        if (pocket.isBottomless()) {
            return amount == null ? 0 : amount;
        }
        if (amount == null || amount == quantity) { // remove ALL ..
            // removed all of the coins from the pocket
            // TODO: config setting to _delete_ zero quantity entries?
            amount = quantity;
            quantity = 0;
        } else if (amount > quantity) {
            throw new InvalidPocketState(true,
                    "Cannot remove %s %s: Pocket %s only contains %s.",
                    amount, name, pocket.name, quantity);
        } else {
            quantity -= amount;
        }
        calculateBaseValue();
        return amount;
    }

    private void calculateBaseValue() {
        baseValue = quantity * unitConversion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PocketCurrency that = (PocketCurrency) o;
        return Objects.equals(pocket, that.pocket)
                && Objects.equals(currency, that.currency);
    }

    @SuppressWarnings("unused")
    public String bulkWeight() { // template
        if (pocket.isBottomless()) {
            return "N/A";
        }
        ProfileConfigData pcd = pocket.profile.config;
        if (pcd.preset == PresetFlavor.pf2e) {
            long bulk = quantity / 1000;
            return pcd.bulkToString(bulk == 0 ? "" : bulk + "");
        }

        return pcd.weightToString((double) quantity / 50);
    }

    public String currentValue() {
        if (pocket.isBottomless()) {
            return "N/A";
        }
        ProfileConfigData pcd = pocket.profile.config;
        return pcd.valueToString(baseValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pocket, currency);
    }
}
