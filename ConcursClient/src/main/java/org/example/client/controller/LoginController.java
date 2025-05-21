package org.example.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.User;
import org.example.services.ConcursServices;
import org.example.services.ServiceException;

import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the login view
 */
public class LoginController {
    private static final Logger logger = LogManager.getLogger();

    private ConcursServices service;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private Button signUpButton;

    /**
     * Set service for this controller
     */
    public void setService(ConcursServices service) {
        this.service = service;
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        logger.info("Login attempt for user: {}", username);

        if (username.isEmpty() || password.isEmpty()) {
            logger.warn("Login failed: empty username or password");
            showAlert("Login Failed", "Username and password cannot be empty.", Alert.AlertType.ERROR);
            return;
        }

        try {
            // Try to authenticate user using service
            Optional<User> authenticatedUser = service.authenticateUser(username, password);

            if (authenticatedUser.isPresent()) {
                User user = authenticatedUser.get();
                logger.info("Login successful for user: {} from office: {}",
                        user.getUsername(), user.getOfficeName());

                // Load main dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
                Parent dashboardRoot = loader.load();

                // Set service and authenticated user for dashboard controller
                DashboardController dashboardController = loader.getController();
                dashboardController.setService(service);
                dashboardController.setCurrentUser(user);

                // Get current stage
                Stage stage = (Stage) loginButton.getScene().getWindow();

                // Set new scene
                Scene scene = new Scene(dashboardRoot, 800, 600);
                stage.setScene(scene);
                stage.setTitle("Swimming Competition Dashboard - " + user.getOfficeName() + " Office");
                stage.centerOnScreen();

            } else {
                logger.warn("Login failed: invalid credentials for user: {}", username);
                showAlert("Login Failed", "Invalid username or password.", Alert.AlertType.ERROR);
            }
        } catch (ServiceException e) {
            logger.error("Error during login", e);
            showAlert("Login Error", "An error occurred during login: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error during login", e);
            showAlert("Login Error", "An unexpected error occurred: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    /**
     * Open the sign up form
     */
    @FXML
    private void openSignUpForm(ActionEvent event) {
        try {
            logger.info("Opening sign up form");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/sign-up.fxml"));
            Parent root = loader.load();

            // Set service for sign up controller
            SignUpController controller = loader.getController();
            controller.setService(service);

            // Create new stage for sign up form
            Stage signUpStage = new Stage();
            signUpStage.initModality(Modality.APPLICATION_MODAL); // Block input to other windows
            signUpStage.setTitle("Create New Account");
            signUpStage.setScene(new Scene(root));
            signUpStage.showAndWait(); // Wait until the sign-up window is closed

        } catch (IOException e) {
            logger.error("Could not open sign up form", e);
            showAlert("Error", "Could not open sign up form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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