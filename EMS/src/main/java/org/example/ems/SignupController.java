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
import java.sql.SQLException;

public class SignupController {

    @FXML private TextField FirstNameText;
    @FXML private TextField LastNameText;
    @FXML private TextField UsernameText;
    @FXML private TextField EmailText;
    @FXML private TextField ContactNumberText;
    @FXML private PasswordField PasswordText;
    @FXML private PasswordField ConfirmPasswordText;
    @FXML private Button BackToLogin;
    @FXML private Label signupMessageLabel;

    @FXML
    private void signupButtonOnAction() {
        if (validateInput()) {
            boolean success = false;
            try {
                success = DatabaseUtil.registerUser(
                        FirstNameText.getText(),
                        LastNameText.getText(),
                        UsernameText.getText(),
                        EmailText.getText(),
                        ContactNumberText.getText(),
                        PasswordText.getText()
                );
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            if (success) {
                signupMessageLabel.setText("Signup successful!");
                // Optionally, redirect to login page
            } else {
                signupMessageLabel.setText("Signup failed. Please try again.");
            }
        }
    }

    @FXML
    private void backToLoginOnAction() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ems/hello-view.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) BackToLogin.getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            signupMessageLabel.setText("Error returning to login page: " + e.getMessage());
        }
    }

    private boolean validateInput() {
        // Implement your input validation logic here
        // Return true if all inputs are valid, false otherwise
        return true; // Placeholder
    }
}