package dev.ebullient.pockets.reference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ItemReference {
    public String name;
    public String idSlug; // key

    public Double weight;
    public String value; // string with units

    public boolean wondrous;
    public String tier;
    public String rarity;
    public String type;

    boolean custom = false; // was this found in an index or created on the fly

    public ItemReference() {
    }

    @JsonIgnore
    public boolean isCustom() {
        return false;
    }
}
