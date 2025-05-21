package org.example.client.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.client.service.ConcursServicesRpcProxy;
import org.example.domain.Event;
import org.example.domain.User;
import org.example.observer.Observer;
import org.example.services.ConcursServices;
import org.example.services.ServiceException;
import org.example.services.dto.EventDTO;
import org.example.services.dto.ParticipantEventDTO;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the main dashboard view
 */
public class DashboardController implements Initializable, Observer {
    private static final Logger logger = LogManager.getLogger();

    private ConcursServices service;
    private User currentUser;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label officeLabel;

    @FXML
    private TableView<EventDTO> eventsTable;

    @FXML
    private TableColumn<EventDTO, String> distanceColumn;

    @FXML
    private TableColumn<EventDTO, String> styleColumn;

    @FXML
    private TableColumn<EventDTO, Integer> participantsColumn;

    @FXML
    private ComboBox<Event> eventComboBox;

    @FXML
    private TableView<ParticipantEventDTO> participantsTable;

    @FXML
    private TableColumn<ParticipantEventDTO, String> nameColumn;

    @FXML
    private TableColumn<ParticipantEventDTO, Integer> ageColumn;

    @FXML
    private TableColumn<ParticipantEventDTO, Integer> eventsCountColumn;

    private ObservableList<EventDTO> eventViewModels = FXCollections.observableArrayList();
    private ObservableList<ParticipantEventDTO> participantViewModels = FXCollections.observableArrayList();

    /**
     * Set services for this controller
     */
    public void setService(ConcursServices service) {
        this.service = service;

        // Register as observer if the service supports it
        if (service instanceof ConcursServicesRpcProxy) {
            logger.info("Registering dashboard as observer");
            ((ConcursServicesRpcProxy) service).addObserver(this);
        }

        // Load initial data
        loadEvents();
    }

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Update UI with user info
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername() + "!");
        }

        if (officeLabel != null) {
            officeLabel.setText("Office: " + user.getOfficeName());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing dashboard");

        // Set up columns
        distanceColumn.setCellValueFactory(new PropertyValueFactory<>("distance"));
        styleColumn.setCellValueFactory(new PropertyValueFactory<>("style"));
        participantsColumn.setCellValueFactory(new PropertyValueFactory<>("participantsCount"));

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        eventsCountColumn.setCellValueFactory(new PropertyValueFactory<>("eventsCount"));

        // Bind data
        eventsTable.setItems(eventViewModels);
        participantsTable.setItems(participantViewModels);

        // Set up event combo box with custom cell factory to display event details
        eventComboBox.setCellFactory(param -> new ListCell<Event>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getDistance() + "m " + item.getStyle());
                }
            }
        });

        // Set up button conversion for display purposes
        eventComboBox.setButtonCell(new ListCell<Event>() {
            @Override
            protected void updateItem(Event item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select an event");
                } else {
                    setText(item.getDistance() + "m " + item.getStyle());
                }
            }
        });

        // Set up event combo box action
        eventComboBox.setOnAction(event -> searchParticipants());
    }

    /**
     * Load events from service and update UI
     */
    private void loadEvents() {
        logger.info("Loading events data");

        if (service != null) {
            try {
                // Get events with participant counts from service
                eventViewModels.setAll(service.getAllEventsWithParticipantCounts());

                // Update event combo box
                ObservableList<Event> eventsList = FXCollections.observableArrayList(service.getAllEvents());
                eventComboBox.setItems(eventsList);

                // Refresh participants if an event is selected
                if (eventComboBox.getValue() != null) {
                    searchParticipants();
                }
            } catch (ServiceException e) {
                logger.error("Error loading events", e);
                showAlert("Error", "Could not load events: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Search for participants in selected event
     */
    @FXML
    private void searchParticipants() {
        Event selectedEvent = eventComboBox.getValue();
        if (selectedEvent != null) {
            logger.info("Searching participants for event: {}", selectedEvent);

            try {
                // Get participants for selected event from service with event counts
                participantViewModels.setAll(service.getParticipantsWithEventCount(selectedEvent.getId()));
            } catch (ServiceException e) {
                logger.error("Error loading participants", e);
                showAlert("Error", "Could not load participants: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    /**
     * Open the registration form
     */
    @FXML
    private void openRegistrationForm(ActionEvent event) {
        try {
            logger.info("Opening registration form");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/register.fxml"));
            Parent root = loader.load();

            // Set services and current user for register controller
            RegisterController controller = loader.getController();
            controller.setService(service);
            controller.setCurrentUser(currentUser);

            // Set up stage
            Stage stage = new Stage();
            stage.setTitle("Register Participant - " + currentUser.getOfficeName() + " Office");
            stage.setScene(new Scene(root, 600, 450));
            stage.showAndWait(); // Modal dialog

            // Refresh data after registration
            loadEvents();

        } catch (IOException e) {
            logger.error("Could not open registration form", e);
            showAlert("Error", "Could not open registration form: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handle logout button click
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            logger.info("Logging out user: {}", currentUser.getUsername());

            // Unregister as observer first
            if (service instanceof ConcursServicesRpcProxy) {
                logger.info("Unregistering dashboard as observer");
                ((ConcursServicesRpcProxy) service).removeObserver(this);
            }

            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent loginRoot = loader.load();

            // Configure login controller
            LoginController loginController = loader.getController();
            loginController.setService(service);

            // Get current stage
            Stage stage = (Stage) eventsTable.getScene().getWindow();

            // Set new scene
            Scene scene = new Scene(loginRoot, 600, 400);
            stage.setScene(scene);
            stage.setTitle("Swimming Competition Registration");
            stage.centerOnScreen();

        } catch (IOException e) {
            logger.error("Could not logout", e);
            showAlert("Error", "Could not logout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Observer update method called when repository data changes
     */
    @Override
    public void update() {
        logger.info("Received update notification in dashboard controller");
        Platform.runLater(this::loadEvents);
    }

    /**
     * Refresh data from service (can be called externally)
     */
    public void refreshData() {
        Platform.runLater(this::loadEvents);
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