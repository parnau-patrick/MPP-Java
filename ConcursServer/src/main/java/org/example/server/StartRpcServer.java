package org.example.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.validator.EventValidator;
import org.example.domain.validator.IValidator;
import org.example.domain.validator.ParticipantValidator;
import org.example.domain.validator.UserValidator;
import org.example.network.utils.AbstractServer;
import org.example.network.utils.ConcursRpcConcurrentServer;
import org.example.persistence.impl.EventRepositoryImpl;
import org.example.persistence.impl.InscriereRepositoryImpl;
import org.example.persistence.impl.ParticipantRepositoryImpl;
import org.example.persistence.impl.UserRepositoryImpl;
import org.example.persistence.interfaces.IEventRepository;
import org.example.persistence.interfaces.IInscriereRepository;
import org.example.persistence.interfaces.IParticipantRepository;
import org.example.persistence.interfaces.IUserRepository;
import org.example.services.ConcursServices;
import org.example.services.ConcursServicesImpl;
import org.example.services.ServiceException;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Main class for starting the RPC server
 */
public class StartRpcServer {
    private static final Logger logger = LogManager.getLogger();
    private static final int DEFAULT_PORT = 55555;

    public static void main(String[] args) {
        Properties serverProps = new Properties();
        try {
            // Try to load properties from file
            try {
                serverProps.load(new FileReader("concursserver.properties"));
                logger.info("Server properties loaded from file");
            } catch (IOException e) {
                // If file not found, try to load from classpath
                try (InputStream is = StartRpcServer.class.getResourceAsStream("/concursserver.properties")) {
                    if (is != null) {
                        serverProps.load(is);
                        logger.info("Server properties loaded from classpath");
                    } else {
                        throw new IOException("Could not find properties in classpath");
                    }
                }
            }
            logger.info("Server properties:");
            serverProps.list(System.out);
        } catch (IOException e) {
            logger.error("Cannot find server properties", e);
            serverProps.setProperty("jdbc.url", "jdbc:sqlite:C:\\ANUL II\\SEMESTRUL II\\MPP\\swim.db");
            serverProps.setProperty("jdbc.user", "");
            serverProps.setProperty("jdbc.pass", "");
            logger.info("Using default SQLite configuration");
        }

        // Create validators
        IValidator eventValidator = new EventValidator();
        IValidator participantValidator = new ParticipantValidator();
        IValidator userValidator = new UserValidator();

        // Create repositories
        IEventRepository eventRepository = new EventRepositoryImpl(serverProps, eventValidator);
        IParticipantRepository participantRepository = new ParticipantRepositoryImpl(serverProps, participantValidator);
        IInscriereRepository inscriereRepository = new InscriereRepositoryImpl(serverProps);
        IUserRepository userRepository = new UserRepositoryImpl(serverProps, userValidator);

        // Create services
        ConcursServices concursServices = new ConcursServicesImpl(
                userRepository, eventRepository, participantRepository, inscriereRepository);

        // Get server port
        int serverPort = DEFAULT_PORT;
        try {
            serverPort = Integer.parseInt(serverProps.getProperty("concurs.server.port"));
        } catch (NumberFormatException e) {
            logger.error("Invalid port number, using default: {}", DEFAULT_PORT);
        }

        logger.info("Starting Concurs RPC server on port: {}", serverPort);
        AbstractServer server = new ConcursRpcConcurrentServer(serverPort, concursServices);

        // Add a shutdown hook to handle ctrl+c gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down server...");
            try {
                server.stop();
                logger.info("Server shut down successfully");
            } catch (ServiceException e) {
                logger.error("Error stopping the server during shutdown", e);
            }
        }));

        try {
            // Start the server - this call is blocking and will only return when the server is stopping
            server.start();
        } catch (ServiceException e) {
            logger.error("Error starting the server", e);
            // Only stop the server if there was an error starting it
            try {
                server.stop();
            } catch (ServiceException stopEx) {
                logger.error("Error stopping the server", stopEx);
            }
        }
    }
}