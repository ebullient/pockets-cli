package dev.ebullient.pockets.actions;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import dev.ebullient.pockets.config.TypeConversion;
import dev.ebullient.pockets.db.ItemDetails;
import dev.ebullient.pockets.db.Posting;
import dev.ebullient.pockets.db.Posting.ItemType;
import dev.ebullient.pockets.db.Posting.PostingType;

@JsonPropertyOrder({ "type", "itemType" })
public class Modification {

    public PostingType type;
    public String fromPocketId; // MOVE, REMOVE
    public String toPocketId; // MOVE, ADD

    public Long quantity;

    public ItemType itemType = Posting.ItemType.ITEM; // default
    public String itemId; // target pocket or item, CREATE
    public String createRef; // reference item used @ create time

    @JsonDeserialize(using = TypeConversion.DeserializeItemDetails.class)
    public ItemDetails itemDetails = null;

    @JsonIgnore
    List<Posting> result = new ArrayList<>();

    public Modification() {
    }

    public Modification create(ItemType type, String itemId, String createRef) {
        this.type = Posting.PostingType.CREATE;
        this.itemType = type;
        this.itemId = itemId;
        this.createRef = createRef;
        return this;
    }

    public Modification add(String itemId, Long quantity, String toPocketId) {
        return this.add(Posting.ItemType.ITEM, itemId, quantity, toPocketId);
    }

    public Modification add(ItemType itemType, String itemId, Long quantity, String toPocketId) {
        this.type = Posting.PostingType.ADD;
        this.itemType = itemType;
        this.itemId = itemId;
        this.quantity = quantity;
        this.toPocketId = toPocketId;
        return this;
    }

    public Modification remove(String itemId, Long quantity, String fromPocketId) {
        return this.remove(Posting.ItemType.ITEM, itemId, quantity, fromPocketId);
    }

    public Modification remove(ItemType itemType, String itemId, Long quantity, String fromPocketId) {
        this.type = Posting.PostingType.REMOVE;
        this.itemType = itemType;
        this.itemId = itemId;
        this.quantity = quantity;
        this.fromPocketId = fromPocketId;
        return this;
    }

    public Modification move(String itemId, Long quantity, String fromPocketId, String toPocketId) {
        this.type = Posting.PostingType.MOVE;
        this.itemId = itemId;
        this.quantity = quantity;
        this.fromPocketId = fromPocketId;
        this.toPocketId = toPocketId;
        return this;
    }

    public Modification setType(Posting.PostingType type) {
        this.type = type;
        return this;
    }

    public Modification setFromPocketId(String fromPocketId) {
        this.fromPocketId = fromPocketId;
        return this;
    }

    public Modification setToPocketId(String toPocketId) {
        this.toPocketId = toPocketId;
        return this;
    }

    public Modification setCreateRef(String createRef) {
        this.createRef = createRef;
        return this;
    }

    public Modification setItemId(String itemId) {
        this.itemId = itemId;
        return this;
    }

    public Modification setItemDetails(ItemDetails itemDetails) {
        this.itemDetails = itemDetails;
        return this;
    }

    public Modification setItemType(ItemType itemType) {
        this.itemType = itemType;
        return this;
    }

    public Modification setQuantity(Long quantity) {
        this.quantity = quantity;
        return this;
    }

    public Posting record(PostingType type) {
        Posting p = new Posting().setType(type);
        result.add(p);
        return p;
    }
}
