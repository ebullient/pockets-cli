package dev.ebullient.pockets.db;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ItemDetailResolver;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PocketRef;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import dev.ebullient.pockets.db.PocketsConverter.PocketDetailsConverter;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;

/**
 * A pocket (or backpack, or haversack, or purse, or ... )
 */
@Entity(name = "Pocket")
@JsonIgnoreProperties({ "profile", "id", "pocketItems", "pocketCurrency" })
@JsonIdentityInfo(scope = Pocket.class, generator = PropertyGenerator.class, property = "slug")
public class Pocket extends PanacheEntity {

    public enum SpecialPocket {
        consumed,
        given,
        loot,
        start;

        public String getId() {
            return "p:" + name();
        }

        public String getRef() {
            return name();
        }

        public static boolean isSpecialPocket(String id) {
            return id.startsWith("p:") || Arrays.stream(values()).anyMatch(v -> id.equals(v.name()));
        }

        public static SpecialPocket find(String value) {
            value = value.toLowerCase();
            for (SpecialPocket sp : values()) {
                if (sp.name().equals(value) || sp.getId().equals(value)) {
                    return sp;
                }
            }
            return null;
        }
    }

    @ManyToOne(optional = false)
    @NaturalId
    Profile profile;

    @NaturalId
    public String slug;

    public String name;

    @Column(length = 5)
    public String emoji;

    @Lob
    @Convert(converter = PocketDetailsConverter.class)
    public PocketDetails pocketDetails;

    @OneToMany(mappedBy = "pocket", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "currency")
    public Map<String, PocketCurrency> pocketCurrency = new HashMap<>();

    /** Items in this pocket */
    @OneToMany(mappedBy = "pocket", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "slug")
    public Map<String, PocketItem> pocketItems = new HashMap<>();

    @OneToMany(mappedBy = "parentPocket")
    @JsonIdentityReference(alwaysAsId = true)
    public Set<Pocket> nestedPockets = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIdentityReference(alwaysAsId = true)
    public Pocket parentPocket;

    @PostLoad
    public void inflatePocketDetails() {
        if (pocketDetails.refId != null && pocketDetails.weight == null) {
            PocketRef ref = profile.config.pocketRef(pocketDetails.refId);
            if (ref != null) {
                ref.applyDefaults(pocketDetails);
                pocketDetails.name = null; // duplicate
                pocketDetails.emoji = null; // duplicate
            }
        }
    }

    @JsonIgnore
    public boolean isBottomless() {
        return Transform.isTrue(pocketDetails.bottomless) || SpecialPocket.isSpecialPocket(slug);
    }

    public void addPocket(Pocket nested) {
        this.nestedPockets.add(nested);
        nested.parentPocket = this;
    }

    public Set<Pocket> getPockets() {
        return this.nestedPockets;
    }

    @SuppressWarnings("unused")
    public void removePocket(Pocket nested) {
        this.nestedPockets.remove(nested);
        nested.parentPocket = null;
    }

    public long addItem(Item i, Long quantity) {
        PocketItem pi = pocketItems.get(i.slug);
        if (pi == null) {
            pi = new PocketItem(this, i.slug);
            i.addPocketItem(pi);
            pocketItems.put(i.slug, pi);
        }
        return pi.add(quantity);
    }

    public long removeItem(Item i, Long quantity) {
        PocketItem pi = pocketItems.get(i.slug);
        if (pi == null) {
            throw new InvalidPocketState(true,
                    "Pocket %s [%s] does not contain %s [%s]",
                    name, slug, i.name, i.slug);
        }
        return pi.remove(quantity);
    }

    public void removeItem(Item i) {
        PocketItem pi = pocketItems.remove(i.slug);
        if (pi == null) {
            throw new InvalidPocketState(true,
                    "Pocket %s [%s] does not contain %s [%s]",
                    name, id, i.name, i.slug);
        }
        pi.pocket = null;
    }

    public long addCurrency(String cid, long quantity) {
        PocketCurrency pc = pocketCurrency.get(cid);
        if (pc == null) {
            pc = new PocketCurrency(this, cid, profile.config.currencyRef(cid));
            pocketCurrency.put(cid, pc);
        }
        pc.deposit(quantity);
        return quantity;
    }

    public long withdraw(String cid, Long quantity) {
        PocketCurrency pc = pocketCurrency.get(cid);
        if (pc == null) {
            if (isBottomless()) {
                pc = new PocketCurrency(this, cid, profile.config.currencyRef(cid));
            } else {
                return 0;
            }
        }
        return pc.withdraw(quantity);
    }

    public void removeCurrency(String cid) {
        PocketCurrency pc = pocketCurrency.remove(cid);
        if (pc == null) {
            throw new InvalidPocketState(true,
                    "Pocket %s [%s] does not contain %s",
                    name, id, cid);
        }
        pc.pocket = null;
    }

    public Pocket setPocketDetails(Profile context, String id, PocketDetails pocketDetails) {
        this.pocketDetails = pocketDetails;
        this.slug = id;
        ItemDetailResolver.unpack(context, this);
        return this;
    }

    public String asMemo(ProfileConfigData pcd) {
        return String.format("%s; pocket; %s%s", name, pocketDetails.asMemo(pcd),
                Transform.isBlank(pocketDetails.notes) ? "" : "; " + pocketDetails.notes);
    }

    @SuppressWarnings("unused")
    public String unitValue() { // template
        // value of a single item
        return profile.config.valueToString(pocketDetails.baseUnitValue);
    }

    @SuppressWarnings("unused")
    public String cumulativeValue() { // template
        double cumulativeValue = pocketCurrency.values().stream()
                .mapToDouble(c -> c.baseValue)
                .sum();

        cumulativeValue += pocketItems.values().stream()
                .filter(pi -> Transform.isTrue(pi.item.itemDetails.tradable)
                        && Transform.isTrue(pi.item.itemDetails.fullValueTrade))
                .mapToDouble(pi -> pi.cumulativeValue)
                .sum();

        return profile.config.valueToString(cumulativeValue);
    }

    @SuppressWarnings("unused")
    public String approximateTradableValue() { // template
        Double cumulativeValue = pocketItems.values().stream()
                .filter(pi -> Transform.isTrue(pi.item.itemDetails.tradable)
                        && Transform.isFalse(pi.item.itemDetails.fullValueTrade))
                .mapToDouble(pi -> Math.round(pi.cumulativeValue / 2))
                .sum();

        return profile.config.valueToString(cumulativeValue);
    }

    @SuppressWarnings("unused")
    public String bulkWeight() { // template
        // bulk or weight of one item (without contents)
        return pocketDetails.bulkWeight(profile.config);
    }

    @SuppressWarnings("unused")
    public String cumulativeBulkWeight() { // template
        if (profile.config.preset == PresetFlavor.pf2e) {
            return cumulativeBulk();
        }
        return profile.config.weightToString(cumulativeWeight());
    }

    public double cumulativeWeight() {
        double weight = pocketItems.values().stream()
                .mapToDouble(x -> x.cumulativeWeight)
                .sum();

        weight += (double) (pocketCurrency.values().stream()
                .mapToLong(x -> x.quantity)
                .sum() / 50);

        return weight;
    }

    public String cumulativeBulk() {
        long bulk = 0;
        long light = 0;

        // coins count as 1 Bulk per thousand coins, no more, no less.
        long coins = pocketCurrency.values().stream()
                .mapToLong(pc -> pc.quantity)
                .sum();
        bulk += coins / 1000;

        for (PocketItem pi : pocketItems.values()) {
            String bulkStr = pi.item.itemDetails.bulk;
            if (bulkStr != null) {
                switch (bulkStr) {
                    case "-":
                    case "negligible":
                        break;
                    case "L":
                    case "light":
                        light += pi.quantity;
                        break;
                    default:
                        bulk += (int) (Integer.parseInt(bulkStr) * pi.quantity);
                }
            }
        }
        bulk += light / 10; // no rounding w/ negligible
        return profile.config.bulkToString(bulk == 0 ? "" : bulk + "");
    }

    public String maxBulkWeight() {
        if (Transform.isTrue(pocketDetails.bottomless)) {
            return "N/A";
        }
        String extradimensional = Transform.isTrue(pocketDetails.magical)
                ? "Extradimensional. "
                : "";

        if (profile.config.preset == PresetFlavor.pf2e) {
            return extradimensional + profile.config.bulkToString(pocketDetails.maxBulk);
        }

        String weight = profile.config.weightToString(pocketDetails.maxWeight);
        String volume = "";
        if (pocketDetails.maxVolume != null) {
            volume = profile.config.volumeToString(pocketDetails.maxVolume);
        } else if (pocketDetails.refId != null) {
            PocketRef ref = profile.config.pocketRef(pocketDetails.refId);
            Double max_volume = ref.compartments.stream()
                    .filter(x -> x.max_volume != null)
                    .mapToDouble(x -> x.max_volume)
                    .sum();
            volume = profile.config.volumeToString(max_volume);
        }
        return extradimensional
                + weight
                + (!weight.isEmpty() && !volume.isEmpty() ? "; " : "")
                + volume;
    }

    public String flags() {
        String result = emoji;
        if (Transform.isTrue(pocketDetails.magical)) {
            result += "🪄";
        }
        if (Transform.isTrue(pocketDetails.bottomless)) {
            result += "∞";
        }
        return result;
    }

    public static Pocket findByNaturalId(Profile profile, String pocketId) {
        Pocket pocket = profile.getPocket(pocketId);
        return pocket == null
                ? Pocket.findByNaturalId(profile.id, pocketId)
                : pocket;
    }

    public static Pocket findByNaturalId(Long profileId, String pocketId) {
        if (SpecialPocket.isSpecialPocket(pocketId)) {
            return findOrCreateSpecialPocket(profileId, pocketId);
        }
        return find(Constants.PROFILE_NATURAL_ID, profileId, pocketId).singleResult();
    }

    public static Optional<Pocket> findByNaturalIdOptional(Long profileId, String pocketId) {
        if (SpecialPocket.isSpecialPocket(pocketId)) {
            return Optional.of(findOrCreateSpecialPocket(profileId, pocketId));
        }
        return find(Constants.PROFILE_NATURAL_ID, profileId, pocketId).singleResultOptional();
    }

    public static List<Pocket> listByNaturalIdLike(Long profileId, String pocketId) {
        if (SpecialPocket.isSpecialPocket(pocketId)) {
            return List.of(findOrCreateSpecialPocket(profileId, pocketId));
        }
        return list(Constants.PROFILE_NATURAL_ID_LIKE, profileId, pocketId);
    }

    static Pocket findOrCreateSpecialPocket(Long profileId, String pocketId) {
        Profile profile = Profile.findById(profileId);

        SpecialPocket sp = SpecialPocket.find(pocketId);
        Pocket specialPocket = profile.getPocket(sp.getId());
        if (specialPocket != null) {
            return specialPocket;
        }
        Optional<Pocket> op = find(Constants.PROFILE_NATURAL_ID, profile.id, sp.getId()).singleResultOptional();
        if (op.isPresent()) {
            return op.get();
        }
        PocketDetails details = new PocketDetails().setRefId(sp.getRef());

        specialPocket = new Pocket();
        specialPocket.setPocketDetails(profile, sp.getId(), details);

        profile.addPocket(specialPocket); // Add pocket to profile (cascade)
        profile.persist(); // ✨

        return specialPocket;
    }
}
