package org.example.client.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.Event;
import org.example.domain.Inscriere;
import org.example.domain.Participant;
import org.example.domain.User;
import org.example.network.dto.RegistrationDTO;
import org.example.network.dto.UserDTO;
import org.example.network.rpcprotocol.Request;
import org.example.network.rpcprotocol.RequestType;
import org.example.network.rpcprotocol.Response;
import org.example.network.rpcprotocol.ResponseType;
import org.example.observer.Observer;
import org.example.services.ConcursServices;
import org.example.services.ServiceException;
import org.example.services.dto.EventDTO;
import org.example.services.dto.ParticipantEventDTO;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Proxy for ConcursServices that communicates with the server via RPC
 */
public class ConcursServicesRpcProxy implements ConcursServices {
    private static final Logger logger = LogManager.getLogger();

    private String host;
    private int port;

    private ObjectInputStream input;
    private ObjectOutputStream output;
    private Socket connection;


    private BlockingQueue<Response> queuedResponses;
    private volatile boolean finished;

    // For observer pattern
    private final List<Observer> observers = new ArrayList<>();

    public ConcursServicesRpcProxy(String host, int port) {
        this.host = host;
        this.port = port;
        this.queuedResponses = new LinkedBlockingQueue<>();
    }

    /**
     * Initialize connection to server
     */
    public void initializeConnection() {
        try {
            connection = new Socket(host, port);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
            finished = false;

            // Start reader thread for processing responses
            startReader();

            logger.info("Connection initialized to {}:{}", host, port);
        } catch (IOException e) {
            logger.error("Error initializing connection", e);
        }
    }

    /**
     * Start a thread that reads responses from the server
     */
    private void startReader() {
        Thread readerThread = new Thread(() -> {
            while (!finished) {
                try {
                    Response response = (Response) input.readObject();
                    logger.info("Response received: {}", response);

                    // Handle notifications from server if needed
                    if (isUpdateResponse(response)) {
                        handleUpdate(response);
                    } else {
                        // Queue response for processing
                        queuedResponses.put(response);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    if (!finished) {
                        logger.error("Error reading response", e);
                    }
                } catch (InterruptedException e) {
                    logger.error("Queue error", e);
                }
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    /**
     * Check if response is a notification
     */
    private boolean isUpdateResponse(Response response) {
        return response.getType() == ResponseType.NEW_EVENT ||
                response.getType() == ResponseType.NEW_PARTICIPANT ||
                response.getType() == ResponseType.NEW_REGISTRATION;
    }

    /**
     * Handle notification from server
     */
    private void handleUpdate(Response response) {
        logger.info("Received update notification: {}", response.getType());

        // Notify all observers
        notifyObservers();
    }

    /**
     * Add an observer to be notified of changes
     */
    public void addObserver(Observer observer) {
        logger.info("Adding observer: {}", observer);
        observers.add(observer);
    }

    /**
     * Remove an observer
     */
    public void removeObserver(Observer observer) {
        logger.info("Removing observer: {}", observer);
        observers.remove(observer);
    }

    /**
     * Notify all observers of changes
     */
    protected void notifyObservers() {
        logger.info("Notifying {} observers", observers.size());
        for (Observer observer : observers) {
            observer.update();
        }
    }

    /**
     * Send request to server and wait for response
     */
    private Response sendAndReceive(Request request) throws ServiceException {
        try {
            logger.info("Sending request: {}", request);
            output.writeObject(request);
            output.flush();

            Response response = queuedResponses.take();
            logger.info("Received response: {}", response);

            if (response.getType() == ResponseType.ERROR) {
                String errorMsg = (String) response.getData();
                throw new ServiceException("Server error: " + errorMsg);
            }

            return response;
        } catch (IOException | InterruptedException e) {
            logger.error("Error sending request", e);
            throw new ServiceException("Error communicating with server: " + e.getMessage(), e);
        }
    }

    /**
     * Close the connection to the server
     */
    public void closeConnection() {
        try {
            finished = true;
            // Send logout request
            Request request = new Request(RequestType.LOGOUT, null);
            sendAndReceive(request);
        } catch (ServiceException e) {
            logger.warn("Error sending logout", e);
        }

        try {
            input.close();
            output.close();
            connection.close();
            logger.info("Connection closed");
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) throws ServiceException {
        UserDTO userDTO = new UserDTO(username, password);
        Request request = new Request(RequestType.LOGIN, userDTO);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            User user = (User) response.getData();
            return Optional.of(user);
        }
        return Optional.empty();
    }

    @Override
    public User registerUser(String username, String password, String officeName) throws ServiceException {
        UserDTO userDTO = new UserDTO(username, password, officeName);
        Request request = new Request(RequestType.REGISTER_USER, userDTO);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            return (User) response.getData();
        }
        // If we reached here, an exception should have been thrown by sendAndReceive
        throw new ServiceException("Unknown error registering user");
    }

    @Override
    public List<Event> getAllEvents() throws ServiceException {
        Request request = new Request(RequestType.GET_EVENTS, null);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            return (List<Event>) response.getData();
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<Event> getEventById(Integer id) throws ServiceException {

        List<Event> events = getAllEvents();
        return events.stream()
                .filter(event -> event.getId().equals(id))
                .findFirst();
    }

    @Override
    public Event createEvent(String distance, String style) throws ServiceException {

        throw new ServiceException("Create event not implemented");
    }

    @Override
    public List<EventDTO> getAllEventsWithParticipantCounts() throws ServiceException {
        Request request = new Request(RequestType.GET_EVENTS_WITH_PARTICIPANT_COUNTS, null);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            return (List<EventDTO>) response.getData();
        }
        return new ArrayList<>();
    }

    @Override
    public List<Participant> getAllParticipants() throws ServiceException {
        Request request = new Request(RequestType.GET_ALL_PARTICIPANTS, null);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            return (List<Participant>) response.getData();
        }
        return new ArrayList<>();
    }

    @Override
    public Optional<Participant> getParticipantById(Integer id) throws ServiceException {
        // We're only getting all participants in this simplified version, so we filter here
        List<Participant> participants = getAllParticipants();
        return participants.stream()
                .filter(participant -> participant.getId().equals(id))
                .findFirst();
    }

    @Override
    public Inscriere registerParticipant(String name, int age, List<Integer> eventIds) throws ServiceException {
        RegistrationDTO registrationDTO = new RegistrationDTO(name, age, eventIds);
        Request request = new Request(RequestType.REGISTER_PARTICIPANT, registrationDTO);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            // We create a basic Inscriere object since the actual one might not be returned
            Participant participant = new Participant(name, age);
            Inscriere inscriere = new Inscriere(participant);
            return inscriere;
        }
        throw new ServiceException("Registration failed");
    }

    @Override
    public List<Event> getEventsByParticipant(Integer participantId) throws ServiceException {
        // Not directly implemented - extend if needed
        throw new ServiceException("Get events by participant not implemented");
    }

    @Override
    public List<Participant> getParticipantsByEvent(Integer eventId) throws ServiceException {
        // Get full participant data with event counts and then map it
        List<ParticipantEventDTO> fullData = getParticipantsWithEventCount(eventId);
        List<Participant> result = new ArrayList<>();

        for (ParticipantEventDTO dto : fullData) {
            Participant participant = new Participant(dto.getName(), dto.getAge());
            participant.setId(dto.getId());
            result.add(participant);
        }

        return result;
    }

    @Override
    public List<ParticipantEventDTO> getParticipantsWithEventCount(Integer eventId) throws ServiceException {
        Request request = new Request(RequestType.GET_PARTICIPANTS_BY_EVENT, eventId);
        Response response = sendAndReceive(request);

        if (response.getType() == ResponseType.OK) {
            return (List<ParticipantEventDTO>) response.getData();
        }
        return new ArrayList<>();
    }
}