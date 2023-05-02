package dev.ebullient.pockets.db;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;

import com.fasterxml.jackson.databind.JsonNode;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.ProfileConfigDataState;
import dev.ebullient.pockets.io.InvalidPocketState;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.validation.constraints.NotNull;
import picocli.CommandLine.ExitCode;

@Entity(name = "Profile")
@NaturalIdCache
public class Profile extends PanacheEntity {
    @NaturalId
    public String slug;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<Journal> journals = new ArrayList<>();

    /** Many pockets belong to this profile */
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "slug")
    protected Map<String, Pocket> pockets = new HashMap<>();

    /** Many items belong to this profile */
    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @MapKeyColumn(name = "slug")
    protected Map<String, Item> items = new HashMap<>();

    @Lob
    @NotNull // serialized config
    @Convert(converter = PocketsConverter.ProfileConfigDataConverter.class)
    public ProfileConfigData config;

    @Override
    public void persist() {
        super.persist();
        status(); // reset status after update
    }

    protected void status() {
        config.state = pockets.isEmpty() && items.isEmpty() && journals.isEmpty()
                ? ProfileConfigDataState.empty
                : ProfileConfigDataState.hasData;
    }

    @PostLoad
    protected void syncId() {
        status(); // set status after load
        config.slug = slug;
    }

    @SuppressWarnings("unused")
    public void addJournal(Journal journal) {
        this.journals.add(journal);
        journal.profile = this;
    }

    public List<Journal> getJournals() {
        return this.journals;
    }

    @SuppressWarnings("unused")
    public void removeJournal(Journal journal) {
        this.journals.remove(journal);
        journal.profile = null;
    }

    @SuppressWarnings("unused")
    public void addPocket(Pocket pocket) {
        this.pockets.put(pocket.slug, pocket);
        pocket.profile = this;
    }

    public Pocket getPocket(String naturalId) {
        return this.pockets.get(naturalId);
    }

    @SuppressWarnings("unused")
    public void removePocket(Pocket pocket) {
        this.pockets.remove(pocket.slug);
        pocket.profile = null;
        // recurse from here. The profile knows all of the pockets..
        List<Pocket> nested = new ArrayList<>(pocket.nestedPockets);
        nested.forEach(this::removePocket);
    }

    @SuppressWarnings("unused")
    public void addItem(Item item) {
        this.items.put(item.slug, item);
        item.profile = this;
    }

    public Item getItem(String naturalId) {
        return this.items.get(naturalId);
    }

    @SuppressWarnings("unused")
    public void removeItem(Item item) {
        this.items.remove(item.slug);
        item.profile = null;
    }

    public JsonNode export() {
        Map<String, Object> map = new HashMap<>();
        map.put("items", items);
        map.put("pockets", pockets);

        return Transform.toJson(map);
    }

    private void reset() {
        config = ProfileConfigData.create(config.slug);
        this.items.clear();
        this.pockets.clear();
        this.journals.clear();
        this.persistAndFlush();
    }

    public static Profile createProfile(ProfileConfigData newCfg) {
        Profile p = new Profile();
        p.slug = newCfg.slug;
        p.config = newCfg;
        p.persistAndFlush();
        return p;
    }

    public static void deleteByNaturalId(String slugId) {
        Profile p = findByNaturalId(slugId);
        if (p.slug.equals("default")) {
            p.reset(); // ✨
        } else {
            p.delete();
        }
    }

    public static ProfileLookupKeys findKeysByNaturalId(String slugId) {
        return find("slug", slugId).project(ProfileLookupKeys.class).singleResult();
    }

    public static Profile findByNaturalId(String slugId) {
        return getEntityManager()
                .unwrap(Session.class)
                .bySimpleNaturalId(Profile.class)
                .load(slugId);
    }

    public static List<String> listByNaturalIdMatching(String match) {
        List<String> allProfiles = listByNaturalId();
        return allProfiles.stream()
                .filter(x -> x.startsWith(match))
                .collect(Collectors.toList());
    }

    public static List<String> listByNaturalId() {
        PanacheQuery<ProfileNaturalId> query = findAll().project(ProfileNaturalId.class);
        return query.stream().map(x -> x.slug).collect(Collectors.toList());
    }

    public static Map<String, ProfileConfigData> listAllProfiles() {
        List<Profile> allProfiles = Profile.listAll();
        Map<String, ProfileConfigData> map = new HashMap<>();
        allProfiles.forEach(entity -> {
            map.put(entity.slug, entity.config);
            Tui.debugf("found profile: %s -> %s", entity.slug, entity.config);
        });
        return map;
    }

    public static Profile updateProfile(ProfileConfigData oldCfg, ProfileConfigData newCfg) {
        if (oldCfg == null || oldCfg.slug == null) {
            return createProfile(newCfg);
        }
        Profile existingEntity = Profile.findByNaturalId(oldCfg.slug);
        existingEntity.config.merge(newCfg);
        existingEntity.persist();
        return existingEntity;
    }

    public static String bulkUpdate(String startingActiveProfile, Map<String, ProfileConfigData> updates) {
        // Validate contents of updated profiles before we persist them (look at all.. )
        if (updates == null) {
            throw new InvalidPocketState(ExitCode.USAGE, "Must provide an argument to bulkUpdate");
        } else if (updates.isEmpty()) {
            // Reset 💥
            List<String> allProfiles = listByNaturalId();
            for (String naturalId : allProfiles) {
                Profile.deleteByNaturalId(naturalId);
            }
            return "default";
        }

        List<String> errors = new ArrayList<>();
        updates.forEach((k, v) -> v.validate(errors));
        if (!errors.isEmpty()) {
            throw new InvalidPocketState(ExitCode.USAGE,
                    "Profile reference types (preset or custom) contain validation errors:%n%s",
                    String.join("\n  ", errors));
        }

        Map<String, ProfileConfigData> existing = Profile.listAllProfiles();
        String activeProfile = startingActiveProfile == null ? "default" : startingActiveProfile;

        Set<String> oldKeys = new HashSet<>(existing.keySet());
        oldKeys.removeAll(updates.keySet());
        if (oldKeys.remove("default")) {
            Profile entity = Profile.findById("default");
            entity.reset();
        }

        Set<String> updateKeys = new HashSet<>(existing.keySet());
        updateKeys.retainAll(updates.keySet());

        Set<String> newKeys = new HashSet<>(updates.keySet());
        newKeys.removeAll(existing.keySet());

        for (String p : oldKeys) {
            ProfileConfigData cfg = existing.remove(p);
            Profile.deleteByNaturalId(cfg.slug); // ✨
            Tui.debugf("bulkUpdate: deleted profile: %s", p);
            if (p.equals(startingActiveProfile)) {
                activeProfile = "default";
                Tui.debugf("bulkUpdate: reset active profile to default");
            }
        }

        for (String p : updateKeys) {
            ProfileConfigData oldCfg = existing.get(p);
            ProfileConfigData newCfg = updates.get(p);
            Profile entity = Profile.updateProfile(oldCfg, newCfg); // ✨
            Tui.debugf("bulkUpdate: updated profile: %s -> %s", entity.id, entity.config);
            if (p.equals(startingActiveProfile)) {
                activeProfile = entity.slug;
                Tui.debugf("bulkUpdate: updated active profile: %s -> %s", entity.id, entity.config);
            }
        }

        for (String p : newKeys) {
            ProfileConfigData cfg = updates.get(p);
            Profile entity = Profile.createProfile(cfg); // ✨
            Tui.debugf("bulkUpdate: added profile: %s -> %s", entity.id, entity.config);
            if (p.equals(startingActiveProfile)) {
                activeProfile = entity.slug;
                Tui.debugf("bulkUpdate: updated active profile: %s -> %s", entity.id, cfg);
            }
        }

        return activeProfile;
    }

    public Collection<Item> allItems() {
        return items.values();
    }

    public Collection<Pocket> allPockets() {
        return pockets.values();
    }

    @RegisterForReflection
    static class ProfileNaturalId {
        public String slug;

        public ProfileNaturalId(String slug) {
            this.slug = slug;
        }
    }

    @RegisterForReflection
    public static class ProfileLookupKeys {
        public Long id;
        public String slug;

        public ProfileLookupKeys(Long id, String slug) {
            this.id = id;
            this.slug = slug;
        }
    }
}
