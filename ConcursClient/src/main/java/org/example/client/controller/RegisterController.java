package org.example.client.controller;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.Event;
import org.example.domain.User;
import org.example.services.ConcursServices;
import org.example.services.ServiceException;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the participant registration view
 */
public class RegisterController implements Initializable {
    private static final Logger logger = LogManager.getLogger();

    private ConcursServices service;
    private User currentUser;

    @FXML
    private TextField nameField;

    @FXML
    private TextField ageField;

    @FXML
    private ListView<Event> availableEventsList;

    @FXML
    private ListView<Event> selectedEventsList;

    @FXML
    private Label officeLabel;

    private List<Event> availableEvents = new ArrayList<>();
    private List<Event> selectedEvents = new ArrayList<>();

    /**
     * Set service for this controller
     */
    public void setService(ConcursServices service) {
        this.service = service;
        // Load events
        loadEvents();
    }

    /**
     * Set the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (officeLabel != null) {
            officeLabel.setText("Office: " + user.getOfficeName());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing registration form");

        // Set up list views
        availableEventsList.setCellFactory(lv -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.getDistance() + "m " + event.getStyle());
                }
            }
        });

        selectedEventsList.setCellFactory(lv -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event event, boolean empty) {
                super.updateItem(event, empty);
                if (empty || event == null) {
                    setText(null);
                } else {
                    setText(event.getDistance() + "m " + event.getStyle());
                }
            }
        });

        // Set double-click handlers
        availableEventsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                addSelectedEvent();
            }
        });

        selectedEventsList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                removeSelectedEvent();
            }
        });
    }

    /**
     * Load events from service
     */
    private void loadEvents() {
        logger.info("Loading events for registration form");

        if (service != null) {
            try {
                availableEvents = service.getAllEvents();
                availableEventsList.setItems(FXCollections.observableArrayList(availableEvents));
            } catch (ServiceException e) {
                logger.error("Error loading events", e);
                showAlert("Error", "Could not load events: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Add selected event to the selected events list
     */
    @FXML
    private void addSelectedEvent() {
        Event selectedEvent = availableEventsList.getSelectionModel().getSelectedItem();
        if (selectedEvent != null && !selectedEvents.contains(selectedEvent)) {
            logger.debug("Adding event to selection: {}", selectedEvent);

            selectedEvents.add(selectedEvent);
            availableEvents.remove(selectedEvent);

            // Update lists
            availableEventsList.setItems(FXCollections.observableArrayList(availableEvents));
            selectedEventsList.setItems(FXCollections.observableArrayList(selectedEvents));
        }
    }

    /**
     * Remove selected event from the selected events list
     */
    @FXML
    private void removeSelectedEvent() {
        Event selectedEvent = selectedEventsList.getSelectionModel().getSelectedItem();
        if (selectedEvent != null) {
            logger.debug("Removing event from selection: {}", selectedEvent);

            selectedEvents.remove(selectedEvent);
            availableEvents.add(selectedEvent);

            // Update lists
            availableEventsList.setItems(FXCollections.observableArrayList(availableEvents));
            selectedEventsList.setItems(FXCollections.observableArrayList(selectedEvents));
        }
    }

    /**
     * Handle registration button click
     */
    @FXML
    private void handleRegister(ActionEvent event) {
        logger.info("Processing participant registration");

        String name = nameField.getText().trim();
        String ageText = ageField.getText().trim();

        if (name.isEmpty() || ageText.isEmpty() || selectedEvents.isEmpty()) {
            logger.warn("Registration validation failed: missing required fields");
            showAlert("Validation Error", "All fields are required and at least one event must be selected.",
                    Alert.AlertType.ERROR);
            return;
        }

        try {
            int age = Integer.parseInt(ageText);

            // Extract event IDs for service
            List<Integer> eventIds = selectedEvents.stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());

            // Register participant using service
            service.registerParticipant(name, age, eventIds);

            logger.info("Registration completed successfully by user: {} from office: {}",
                    currentUser.getUsername(), currentUser.getOfficeName());
            showAlert("Success", "Participant registered successfully.", Alert.AlertType.INFORMATION);
            closeWindow();

        } catch (NumberFormatException e) {
            logger.error("Invalid age format", e);
            showAlert("Validation Error", "Age must be a valid number.", Alert.AlertType.ERROR);
        } catch (ServiceException e) {
            logger.error("Service error during registration", e);
            showAlert("Error", "An error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            showAlert("Error", "An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle cancel button click
     */
    @FXML
    private void handleCancel(ActionEvent event) {
        logger.info("Registration cancelled");
        closeWindow();
    }

    /**
     * Close this window
     */
    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
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