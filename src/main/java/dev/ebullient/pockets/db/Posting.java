package dev.ebullient.pockets.db;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.ebullient.pockets.config.TypeConversion.DeserializeItemClass;

@JsonPropertyOrder({ "type", "itemType" })
public class Posting {
    public static final TypeReference<List<Posting>> LIST_POSTING = new TypeReference<>() {
    };

    public enum PostingType {
        MOVE,
        ADD,
        REMOVE,
        CREATE
    }

    public enum ItemType {
        ITEM,
        POCKET,
        CURRENCY
    }

    public PostingType type;
    public ItemType itemType = ItemType.ITEM;

    public String pocketId;
    public String itemId;
    public Long quantity;

    @JsonDeserialize(using = DeserializeItemClass.class)
    public Object created;

    public Posting setItemType(ItemType itemType) {
        this.itemType = itemType;
        return this;
    }

    public Posting setPocketId(String pocketId) {
        this.pocketId = pocketId;
        return this;
    }

    public Posting setQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Posting setCreated(Object created) {
        this.created = created;
        return this;
    }

    public Posting setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public Posting setType(PostingType type) {
        this.type = type;
        return this;
    }
}
