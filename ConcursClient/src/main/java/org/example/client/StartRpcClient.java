package org.example.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.client.controller.LoginController;
import org.example.client.service.ConcursServicesRpcProxy;
import org.example.services.ConcursServices;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main class for starting the RPC client
 */
public class StartRpcClient extends Application {
    private static final Logger logger = LogManager.getLogger();
    private static final String DEFAULT_SERVER_HOST = "localhost";
    private static final int DEFAULT_SERVER_PORT = 55555;

    private ConcursServicesRpcProxy serviceProxy;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting client application");

            // Load client properties
            Properties clientProps = new Properties();
            try {
                // Try to load from file
                try {
                    clientProps.load(new FileReader("concursclient.properties"));
                    logger.info("Client properties loaded from file");
                } catch (IOException e) {
                    // If file not found, try to load from classpath
                    try (InputStream is = StartRpcClient.class.getResourceAsStream("/concursclient.properties")) {
                        if (is != null) {
                            clientProps.load(is);
                            logger.info("Client properties loaded from classpath");
                        } else {
                            throw new IOException("Could not find properties in classpath");
                        }
                    }
                }
            } catch (IOException e) {
                logger.warn("Cannot find client properties file", e);
                logger.info("Using default connection settings");
                clientProps.setProperty("concurs.server.host", DEFAULT_SERVER_HOST);
                clientProps.setProperty("concurs.server.port", String.valueOf(DEFAULT_SERVER_PORT));
            }

            // Initialize service proxy
            String serverHost = clientProps.getProperty("concurs.server.host", DEFAULT_SERVER_HOST);
            int serverPort = DEFAULT_SERVER_PORT;
            try {
                serverPort = Integer.parseInt(clientProps.getProperty("concurs.server.port"));
            } catch (NumberFormatException e) {
                logger.error("Invalid port, using default: {}", DEFAULT_SERVER_PORT);
            }

            logger.info("Connecting to server {}:{}", serverHost, serverPort);
            serviceProxy = new ConcursServicesRpcProxy(serverHost, serverPort);
            serviceProxy.initializeConnection();

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
            Parent root = loader.load();

            // Set service for login controller
            LoginController loginController = loader.getController();
            loginController.setService(serviceProxy);

            // Set up primary stage
            primaryStage.setTitle("Swimming Competition Registration");
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.show();

            // Set up close request handling
            primaryStage.setOnCloseRequest(event -> {
                logger.info("Closing client application");
                if (serviceProxy != null) {
                    serviceProxy.closeConnection();
                }
                Platform.exit();
                System.exit(0);
            });

        } catch (Exception e) {
            logger.error("Error starting client", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot start client");
            alert.setContentText("Error connecting to server: " + e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        logger.info("Stopping client application");
        if (serviceProxy != null) {
            serviceProxy.closeConnection();
        }
    }
}