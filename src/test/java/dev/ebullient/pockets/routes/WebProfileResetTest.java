package dev.ebullient.pockets.routes;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.config.Types.PocketConfigData;
import dev.ebullient.pockets.config.Types.PresetFlavor;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(CamelQuarkusTestSupport.WebProfileResetTest.class)
public class WebProfileResetTest extends CamelQuarkusTestSupport {

    // Transactions happen in the rest request (and are committed), so TestTransaction
    // doesn't work here. Using a separate profile to force restart.

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    ProfileContext profileContext;

    @Test
    public void testResetEndpoint() {
        PocketConfigData data = given()
                .when()
                .get("/config/current")
                .then()
                .statusCode(200)
                .extract().as(PocketConfigData.class);

        // reset everything
        data.profiles.clear();
        assertThat(data.profiles).hasSize(0);
        data = given()
                .body(data)
                .contentType(ContentType.JSON)
                .when()
                .put("/config/current")
                .then()
                .statusCode(200)
                .extract().as(PocketConfigData.class);

        assertThat(data.activeProfile).matches("default");
        assertThat(data.profiles).hasSize(1);
        assertThat(data.profiles.get("default").preset).isNotEqualTo(PresetFlavor.dnd5e);
    }
}
