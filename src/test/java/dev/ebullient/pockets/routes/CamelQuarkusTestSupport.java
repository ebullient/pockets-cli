package dev.ebullient.pockets.routes;

import static dev.ebullient.pockets.io.PocketTui.Tui;

import org.apache.camel.quarkus.main.CamelMain;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import dev.ebullient.pockets.Transform;
import io.quarkus.arc.Arc;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;

public class CamelQuarkusTestSupport {

    public static class WebProfileConfigTest implements QuarkusTestProfile {
    }

    public static class WebProfileResetTest implements QuarkusTestProfile {
    }

    public static class WebProfileEndpointTest implements QuarkusTestProfile {
    }

    public static class WebModifyPocketTest implements QuarkusTestProfile {
    }

    public static class EmitterRouteTest implements QuarkusTestProfile {
    }

    @BeforeAll
    public static void beforeAll() throws Exception {
        Tui.init(true, true);

        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(new ObjectMapperConfig()
                .jackson2ObjectMapperFactory((cls, charset) -> Transform.JSON));

        CamelMain main = Arc.container().instance(CamelMain.class).get();
        if (!main.isStarted()) {
            main.startEngine();
        }
    }

    @BeforeEach
    void beforeEachTest(TestInfo testInfo) throws Exception {
        if (testInfo.getTestClass().isPresent()
                && testInfo.getTestClass().get().getSimpleName().startsWith("Web")) {
            CamelMain main = Arc.container().instance(CamelMain.class).get();
            main.getCamelContext().getRouteController().startAllRoutes();
        }
    }

    @AfterAll
    public static void afterAll() {
        CamelMain main = Arc.container().instance(CamelMain.class).get();
        main.stop();
    }
}
