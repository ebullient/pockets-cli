package dev.ebullient.pockets.db;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.annotations.NaturalId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ItemDetailResolver;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.ItemRef;
import dev.ebullient.pockets.db.PocketsConverter.ItemDetailsConverter;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;

@Entity(name = "Item")
@JsonIgnoreProperties({ "profile", "id", "pocketItems" })
@JsonIdentityInfo(scope = Item.class, generator = PropertyGenerator.class, property = "slug")
public class Item extends PanacheEntity {
    @ManyToOne(optional = false)
    @NaturalId
    Profile profile;

    @NaturalId
    public String slug;

    public String name;

    @Lob
    @Convert(converter = ItemDetailsConverter.class)
    public ItemDetails itemDetails;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, orphanRemoval = true)
    public Set<PocketItem> pocketItems = new HashSet<>();

    @PostLoad
    public void inflateItemDetails() {
        if (itemDetails.refId != null && itemDetails.tradable == null) {
            ItemRef ref = profile.config.itemRef(itemDetails.refId);
            if (ref != null) {
                ref.applyDefaults(itemDetails);
                itemDetails.name = null; // duplicate
            }
        }
    }

    public Item setItemDetails(Profile context, String slugId, ItemDetails itemDetails) {
        this.slug = slugId;
        this.itemDetails = itemDetails;
        ItemDetailResolver.unpack(context, this);
        return this;
    }

    public void addPocketItem(PocketItem pi) {
        this.pocketItems.add(pi);
        pi.item = this;
    }

    public void removePocketItem(PocketItem pi) {
        this.pocketItems.remove(pi);
        pi.item = null;
    }

    public String asMemo(ProfileConfigData pcd) {
        return String.format("%s; item; %s%s", name, itemDetails.asMemo(pcd),
                Transform.isBlank(itemDetails.notes) ? "" : "; " + itemDetails.notes);
    }

    public String flags() {
        String result = "";
        if (Transform.isTrue(itemDetails.magical)) {
            result += "🪄";
        }
        if (Transform.isTrue(itemDetails.fullValueTrade)) {
            result += "💰";
        } else if (Transform.isTrue(itemDetails.tradable)) {
            result += "💸";
        }
        return result;
    }

    @SuppressWarnings("unused")
    public String bulkWeight() { // templates
        // bulk or weight of one item
        return itemDetails.bulkWeight(profile.config);
    }

    @SuppressWarnings("unused")
    public String unitValue() { // templates
        // value of a single item
        return profile.config.valueToString(itemDetails.baseUnitValue);
    }

    @JsonIgnore
    public String getPockets() {
        return pocketItems.stream()
                .sorted(Comparator.comparing(pi -> pi.pocket.name))
                .map(pi -> String.format("[%s](pocket-%s.md) (%s)", pi.pocket.name, pi.pocket.slug, pi.quantity))
                .collect(Collectors.joining(", "));
    }

    public static Item findByNaturalId(Profile profile, String itemId) {
        Item item = profile.getItem(itemId);
        return item == null
                ? Item.findByNaturalId(profile.id, itemId)
                : item;
    }

    public static Item findByNaturalId(Long profileId, String itemId) {
        return find(Constants.PROFILE_NATURAL_ID, profileId, itemId).singleResult();
    }

    public static Optional<Item> findByNaturalIdOptional(Long profileId, String itemId) {
        return find(Constants.PROFILE_NATURAL_ID, profileId, itemId).singleResultOptional();
    }

    public static List<Item> listByNaturalIdLike(Long id, String itemId) {
        return list(Constants.PROFILE_NATURAL_ID_LIKE, id, itemId);
    }
}
