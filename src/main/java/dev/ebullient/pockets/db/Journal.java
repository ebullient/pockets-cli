package dev.ebullient.pockets.db;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity(name = "Journal")
@JsonIgnoreProperties({ "id", "profile" })
public class Journal extends PanacheEntity {

    /** This belongs to a profile (tenant) */
    @ManyToOne(optional = false)
    @JoinColumn(name = "profile_id")
    Profile profile;

    /** Any notation of when */
    public String datetime;

    /** Memo / description / log */
    public String memo;

    @NotNull
    public UUID uuid;

    /** Many postings belong to this ledger */
    @Lob
    @NotNull
    @Basic(fetch = FetchType.LAZY)
    @Convert(converter = PocketsConverter.PostingDataConverter.class)
    public List<Posting> changes; // serialized posting data

    public Journal setDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    public Journal setMemo(String memo) {
        this.memo = memo;
        return this;
    }

    public Journal setChanges(List<Posting> changes) {
        this.changes = changes;
        return this;
    }

    public Journal setUuid(UUID uuid) {
        this.uuid = uuid;
        return this;
    }
}
