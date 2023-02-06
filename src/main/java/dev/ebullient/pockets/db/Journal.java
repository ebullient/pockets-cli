package dev.ebullient.pockets.db;

import java.util.List;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

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
