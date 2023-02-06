package dev.ebullient.pockets.actions;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModificationRequest {

    public String datetime;
    public String memo;
    public UUID uuid;

    public List<Modification> changes = new ArrayList<>();

    public ModificationRequest() {
        this.uuid = UUID.randomUUID();
        this.datetime = Instant.now()
                .atOffset(ZoneOffset.UTC)
                .format(ISO_LOCAL_DATE_TIME)
                .replace("T", "");
    }

    public ModificationRequest(String memo, String datetime) {
        this.uuid = UUID.randomUUID();
        this.memo = memo;
        this.datetime = datetime;
    }

    public ModificationRequest add(Modification modification) {
        changes.add(modification);
        return this;
    }

    public ModificationRequest setDatetime(String datetime) {
        this.datetime = datetime;
        return this;
    }

    public ModificationRequest setMemo(String memo) {
        this.memo = memo;
        return this;
    }
}
