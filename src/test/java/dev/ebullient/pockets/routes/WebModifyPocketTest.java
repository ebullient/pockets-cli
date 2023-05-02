package dev.ebullient.pockets.routes;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import dev.ebullient.pockets.Transform;
import dev.ebullient.pockets.actions.Modification;
import dev.ebullient.pockets.actions.ModificationRequest;
import dev.ebullient.pockets.actions.ModificationResponse;
import dev.ebullient.pockets.actions.ModifyPockets;
import dev.ebullient.pockets.actions.ProfileContext;
import dev.ebullient.pockets.db.Posting.ItemType;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;

@QuarkusTest
@TestProfile(CamelQuarkusTestSupport.WebModifyPocketTest.class)
public class WebModifyPocketTest extends CamelQuarkusTestSupport {

    @Inject
    ModifyPockets modifyPockets;

    @Inject
    ProfileContext profileContext;

    @Test
    public void testModifyPocket() {
        String profilePath = "/pockets/test-5e";
        ModificationRequest req = new ModificationRequest("New backpack", "1499-03-24");
        req.add(new Modification()
                .create(ItemType.POCKET, "backpack-of-doom", "backpack"));

        ModificationResponse resp = given()
                .body(req)
                .contentType(ContentType.JSON)
                .when()
                .post(profilePath)
                .then()
                .statusCode(200)
                .extract().as(ModificationResponse.class);

        assertThat(resp.changes).isNotNull();
        assertThat(resp.changes.size()).isEqualTo(req.changes.size());

        System.out.println(Transform.toJsonString(resp));
    }
}
