package org.example.network.rpcprotocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.Event;
import org.example.domain.Participant;
import org.example.domain.User;
import org.example.network.dto.RegistrationDTO;
import org.example.network.dto.UserDTO;
import org.example.network.utils.ConcursRpcConcurrentServer;
import org.example.services.ConcursServices;
import org.example.services.ServiceException;
import org.example.services.dto.EventDTO;
import org.example.services.dto.ParticipantEventDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

/**
 * Worker for handling client RPC requests
 */
public class ConcursClientRpcReflectionWorker implements Runnable {
    private static final Logger logger = LogManager.getLogger();
    private final ConcursServices server;
    private final Socket connection;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    public ConcursClientRpcReflectionWorker(ConcursServices server, Socket connection) {
        this.server = server;
        this.connection = connection;
        try {
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            connected = true;
            logger.info("Worker created for {}", connection.getInetAddress());
        } catch (IOException e) {
            logger.error("Error creating worker: {}", e.getMessage());
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object request = input.readObject();
                Response response = handleRequest((Request) request);
                if (response != null) {
                    sendResponse(response);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Error processing client request: {}", e.getMessage());
                connected = false;
            }
        }
        try {
            input.close();
            output.close();
            connection.close();
        } catch (IOException e) {
            logger.error("Error closing connection: {}", e.getMessage());
        }
    }

    private Response handleRequest(Request request) {
        Response response = null;
        try {
            logger.info("Processing request: {}", request);
            RequestType type = request.getType();

            switch (type) {
                case LOGIN:
                    return handleLogin(request);
                case LOGOUT:
                    return handleLogout(request);
                case GET_EVENTS:
                    return handleGetEvents();
                case GET_PARTICIPANTS_BY_EVENT:
                    return handleGetParticipantsByEvent(request);
                case REGISTER_PARTICIPANT:
                    return handleRegisterParticipant(request);
                case GET_EVENTS_WITH_PARTICIPANT_COUNTS:
                    return handleGetEventsWithCounts();
                case GET_ALL_PARTICIPANTS:
                    return handleGetAllParticipants();
                case REGISTER_USER:
                    return handleRegisterUser(request);
            }
        } catch (ServiceException e) {
            logger.error("Service exception: {}", e.getMessage());
            connected = false;
            return new Response(ResponseType.ERROR, e.getMessage());
        }
        return response;
    }

    private Response handleLogin(Request request) throws ServiceException {
        logger.info("Login request...");
        UserDTO userDTO = (UserDTO) request.getData();
        try {
            Optional<User> user = server.authenticateUser(userDTO.getUsername(), userDTO.getPassword());
            if (user.isPresent()) {
                return new Response(ResponseType.OK, user.get());
            } else {
                return new Response(ResponseType.ERROR, "Authentication failed");
            }
        } catch (Exception e) {
            logger.error("Error handling login: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleLogout(Request request) {
        logger.info("Logout request...");
        // User logout logic if needed
        connected = false;
        return new Response(ResponseType.OK, null);
    }

    private Response handleGetEvents() {
        logger.info("Get events request...");
        try {
            List<Event> events = server.getAllEvents();
            return new Response(ResponseType.OK, events);
        } catch (Exception e) {
            logger.error("Error getting events: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleGetParticipantsByEvent(Request request) {
        logger.info("Get participants by event...");
        Integer eventId = (Integer) request.getData();
        try {
            List<ParticipantEventDTO> participants = server.getParticipantsWithEventCount(eventId);
            return new Response(ResponseType.OK, participants);
        } catch (Exception e) {
            logger.error("Error getting participants: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleRegisterParticipant(Request request) {
        logger.info("Register participant request...");
        RegistrationDTO registrationDTO = (RegistrationDTO) request.getData();
        try {
            server.registerParticipant(
                    registrationDTO.getParticipantName(),
                    registrationDTO.getParticipantAge(),
                    registrationDTO.getEventIds());

            // Send notification to all clients about the new registration
            notifyAllClientsAboutNewRegistration();

            return new Response(ResponseType.OK, null);
        } catch (Exception e) {
            logger.error("Error registering participant: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleGetEventsWithCounts() {
        logger.info("Get events with counts request...");
        try {
            List<EventDTO> events = server.getAllEventsWithParticipantCounts();
            return new Response(ResponseType.OK, events);
        } catch (Exception e) {
            logger.error("Error getting events with counts: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleGetAllParticipants() {
        logger.info("Get all participants request...");
        try {
            List<Participant> participants = server.getAllParticipants();
            return new Response(ResponseType.OK, participants);
        } catch (Exception e) {
            logger.error("Error getting participants: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private Response handleRegisterUser(Request request) {
        logger.info("Register user request...");
        UserDTO userDTO = (UserDTO) request.getData();
        try {
            User user = server.registerUser(
                    userDTO.getUsername(),
                    userDTO.getPassword(),
                    userDTO.getOfficeName());
            return new Response(ResponseType.OK, user);
        } catch (Exception e) {
            logger.error("Error registering user: {}", e.getMessage());
            return new Response(ResponseType.ERROR, e.getMessage());
        }
    }

    private void sendResponse(Response response) throws IOException {
        logger.info("Sending response: {}", response);
        output.writeObject(response);
        output.flush();
    }

    /**
     * Send a notification to this client about an update
     */
    public void sendNotification(Response response) {
        try {
            logger.info("Sending notification to client: {}", response);
            output.writeObject(response);
            output.flush();
        } catch (IOException e) {
            logger.error("Error sending notification", e);
        }
    }

    /**
     * Notify all clients about a new registration
     */
    private void notifyAllClientsAboutNewRegistration() {
        logger.info("Notifying all clients about new registration");
        Response notification = new Response(ResponseType.NEW_REGISTRATION, null);

        // Get all clients from the server and notify them
        ConcursRpcConcurrentServer.notifyAllClients(notification);
    }
}