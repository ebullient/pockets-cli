package dev.ebullient.pockets.db;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.config.ProfileConfig;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.logging.Log;

@Entity(name = EntityConstants.PROFILE_CONFIG_ENTITY)
@Table(name = EntityConstants.PROFILE_CONFIG_TABLE)
public class ProfileConfigEntity extends PanacheEntity {

    // @Column(unique = true, nullable = false, length = 50)
    // @Size(min = 1, max = 50)
    public String name; // identifier: name-as-slug

    @NotNull
    public String value;

    @Override
    public void persist() {
        name = Transform.slugify(name);
        super.persist();
    }

    @Override
    public void persistAndFlush() {
        name = Transform.slugify(name);
        super.persistAndFlush();
    }

    /**
     * Find config by slugified name
     *
     * @param name -- will be slugified
     * @return List of pockets that match the slugified name
     */
    public static ProfileConfig findByName(String name) {
        final String query = Transform.slugify(name);
        List<ProfileConfigEntity> allProfiles = ProfileConfigEntity.listAll();
        return allProfiles.stream()
                .filter(p -> p.name.equals(query))
                .map(p -> {
                    try {
                        return Transform.FROM_JSON.readValue(p.value, ProfileConfig.class);
                    } catch (JsonProcessingException e) {
                        Log.errorf("Unable to parse configuration for profile %s: %s", p.name, p.value);
                    }
                    return null;
                })
                .findFirst().orElse(null);
    }
}
