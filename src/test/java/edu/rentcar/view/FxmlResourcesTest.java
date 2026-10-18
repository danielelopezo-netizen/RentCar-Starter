package edu.rentcar.view;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that all FXML views and CSS stylesheets exist in the classpath and bind to valid controller classes.
 */
class FxmlResourcesTest {

    @Test
    void allFxmlViewsAndCssResourcesShouldExistAndHaveValidControllers() throws Exception {
        String[] fxmlFiles = {
                "/edu/rentcar/view/main-view.fxml",
                "/edu/rentcar/view/dashboard-view.fxml",
                "/edu/rentcar/view/customer-view.fxml",
                "/edu/rentcar/view/vehicle-view.fxml",
                "/edu/rentcar/view/reservation-view.fxml"
        };

        for (String path : fxmlFiles) {
            URL resource = getClass().getResource(path);
            assertNotNull(resource, "Missing FXML resource: " + path);
            try (var stream = resource.openStream()) {
                String content = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                assertTrue(content.contains("fx:controller=\"edu.rentcar.controller."),
                        "FXML " + path + " must declare a valid fx:controller");
            }
        }

        URL cssResource = getClass().getResource("/edu/rentcar/view/styles.css");
        assertNotNull(cssResource, "Missing CSS resource: /edu/rentcar/view/styles.css");
    }
}
