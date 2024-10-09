package org.example.ems;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HomePageController {

    private static final Logger LOGGER = Logger.getLogger(HomePageController.class.getName());

    @FXML
    private void logoutButtonOnAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ems/hello-view.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) (event.getSource())).getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error during logout process", e);
            // Handle the error appropriately, e.g., show an error message to the user
        }
    }

    // Add other methods to handle button clicks and other interactions on the homepage
}