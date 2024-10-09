package org.example.ems;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    @FXML private TextField UserText;
    @FXML private PasswordField PassText;
    @FXML private Button Login;
    @FXML private Button Signup;
    @FXML private Label loginMessageLabel;

    @FXML
    private void loginButtonOnAction() {
        if (UserText.getText().isEmpty() || PassText.getText().isEmpty()) {
            loginMessageLabel.setText("Please enter both username and password");
        } else {
            boolean isValid = DatabaseUtil.validateLogin(UserText.getText(), PassText.getText());
            if (isValid) {
                loginMessageLabel.setText("Login successful!");
                navigateToHomePage();
            } else {
                loginMessageLabel.setText("Invalid username or password");
            }
        }
    }

    @FXML
    private void signupButtonOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ems/sign-up.fxml"));
            Parent signupRoot = loader.load();

            Stage stage = (Stage) Signup.getScene().getWindow();
            Scene scene = new Scene(signupRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Error loading signup page: " + e.getMessage());
        }
    }

    private void navigateToHomePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ems/home-page.fxml"));
            Parent homeRoot = loader.load();

            Stage stage = (Stage) Login.getScene().getWindow();
            Scene scene = new Scene(homeRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            loginMessageLabel.setText("Error loading home page: " + e.getMessage());
        }
    }
}