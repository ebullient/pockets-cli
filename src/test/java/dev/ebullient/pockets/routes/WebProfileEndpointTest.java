package dev.ebullient.pockets.routes;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.actions.ProfileContext;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(CamelQuarkusTestSupport.WebProfileEndpointTest.class)
public class WebProfileEndpointTest extends CamelQuarkusTestSupport {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    ProfileContext profileContext;

    @Test
    public void testProfileList() {
        String profilePath = "/pockets";

        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(profilePath)
                .then()
                .statusCode(200)
                .extract().body().asString();

        assertThat(response).isEqualTo("[\"default\",\"test-5e\",\"test-pf2e\"]");
    }

    @Test
    @TestTransaction
    public void testPocketStatus() {
        String profilePath = "/pockets/test-5e";

        String response = given()
                .contentType(ContentType.JSON)
                .when()
                .get(profilePath)
                .then()
                .statusCode(200)
                .extract().asString();

        System.out.println(response);
    }
}
