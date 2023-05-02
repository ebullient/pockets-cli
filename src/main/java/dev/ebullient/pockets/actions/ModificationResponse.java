package dev.ebullient.pockets.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import dev.ebullient.pockets.db.Journal;
import dev.ebullient.pockets.db.Posting;

public class ModificationResponse {
    public String datetime;
    public String memo;
    public UUID uuid;
    public List<Posting> changes = new ArrayList<>();

    @JsonIgnore
    public String profileName;

    public ModificationResponse() {
        uuid = UUID.randomUUID();
    }

    public ModificationResponse(ModificationRequest req) {
        this.datetime = req.datetime;
        this.memo = req.memo;
        this.uuid = req.uuid == null ? UUID.randomUUID() : req.uuid;
    }

    public ModificationResponse setProfile(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public void post(Posting update) {
        changes.add(update);
    }

    public void post(List<Posting> results) {
        changes.addAll(results);
    }

    public Journal toJournal() {
        return new Journal()
                .setUuid(uuid)
                .setDatetime(datetime)
                .setMemo(memo)
                .setChanges(changes);
    }
}
