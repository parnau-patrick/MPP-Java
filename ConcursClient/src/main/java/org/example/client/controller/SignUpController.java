package org.example.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.User;
import org.example.services.ConcursServices;
import org.example.services.ServiceException;

/**
 * Controller for the sign up view
 */
public class SignUpController {
    private static final Logger logger = LogManager.getLogger();

    private ConcursServices service;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField officeNameField;

    /**
     * Set service for this controller
     */
    public void setService(ConcursServices service) {
        this.service = service;
    }

    /**
     * Handle create account button click
     */
    @FXML
    private void handleCreateAccount(ActionEvent event) {
        if (validateForm()) {
            try {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String officeName = officeNameField.getText().trim();

                // Register user using service
                User newUser = service.registerUser(username, password, officeName);

                logger.info("New user created successfully: {}", username);
                showAlert("Success", "Account created successfully! You can now log in.",
                        Alert.AlertType.INFORMATION);

                // Close the form
                closeForm();

            } catch (ServiceException e) {
                logger.error("Service error during user creation", e);
                showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            } catch (Exception e) {
                logger.error("Unexpected error during user creation", e);
                showAlert("Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        closeForm();
    }

    /**
     * Validate the form input
     * @return True if form is valid, false otherwise
     */
    private boolean validateForm() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();
        String officeName = officeNameField.getText().trim();

        // Check for empty fields
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || officeName.isEmpty()) {
            showAlert("Validation Error", "All fields are required.", Alert.AlertType.ERROR);
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert("Validation Error", "Passwords do not match.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    /**
     * Close the form
     */
    private void closeForm() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    /**
     * Show an alert dialog
     */
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}