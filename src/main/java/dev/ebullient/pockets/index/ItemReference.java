package dev.ebullient.pockets.index;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import dev.ebullient.pockets.db.Currency;
import dev.ebullient.pockets.db.Item;
import dev.ebullient.pockets.db.Mapper;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ItemReference {
    public String name;

    @JsonIgnore
    public String idSlug; // key

    public Double weight;
    public Integer quantity;
    public String value; // string with units

    public boolean wondrous;
    public String tier;
    public String rarity;
    public String type;

    @JsonIgnore
    boolean custom = false; // was this found in an index or created on the fly

    public ItemReference() {
    }

    public boolean isCustom() {
        return custom;
    }

    public Item createItem() {
        return createItem(null);
    }

    public Item createItem(String itemName) {
        Item item = new Item();

        item.name = itemName == null
                ? this.name
                : itemName;

        item.slug = Mapper.slugify(item.name);
        item.itemRef = this.idSlug;
        item.weight = this.weight;
        item.quantity = this.quantity == null ? 1 : this.quantity;
        item.cpValue = Currency.cpValue(this.value).orElse(null);
        item.tradable = this.value != null; // if it has a currency value, it is tradeable

        return item;
    }
}
