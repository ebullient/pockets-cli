package dev.ebullient.pockets.routes;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.config.ProfileConfigData;
import dev.ebullient.pockets.config.Types.PocketConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(CamelQuarkusTestSupport.WebProfileConfigTest.class)
public class WebProfileConfigTest extends CamelQuarkusTestSupport {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    ProfileContext profileContext;

    @Test
    public void testConfigCurrentEndpoint() {
        PocketConfigData data = given()
                .when()
                .get("/config/current")
                .then()
                .statusCode(200)
                .extract().as(PocketConfigData.class);

        // Get initialized / default config
        assertThat(data.activeProfile).isEqualTo("default");
        assertThat(data.profiles).containsKeys(data.activeProfile);

        // Add a new profile
        ProfileConfigData newProfile = ProfileConfigData.create("profile-1");
        data.activeProfile = newProfile.slug;
        data.profiles.put(newProfile.slug, newProfile);
        data = given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .put("/config/current")
                .then()
                .statusCode(200)
                .extract().as(PocketConfigData.class);

        String profileKey = data.activeProfile;
        assertThat(profileKey).matches("profile-1");
        assertThat(data.profiles).containsKeys(data.activeProfile);
        assertThat(data.profiles).hasSize(4);

        // rename the profile: will come back unchanged (immutable)
        data.profiles.get(profileKey).slug = "other";
        data.profiles.get("default").preset = PresetFlavor.dnd5e;
        data = given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .put("/config/current")
                .then()
                .statusCode(200)
                .extract().as(PocketConfigData.class);

        assertThat(data.activeProfile).matches("profile-1");
        assertThat(data.activeProfile).isEqualTo(profileKey);

        assertThat(data.profiles).hasSize(4);
        assertThat(data.profiles).containsKey(profileKey);
        assertThat(data.profiles).containsKeys(data.activeProfile);
        assertThat(data.profiles.get("default").preset).isEqualTo(PresetFlavor.dnd5e);
    }
}
