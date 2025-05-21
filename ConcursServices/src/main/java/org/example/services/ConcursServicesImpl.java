package org.example.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.Event;
import org.example.domain.Inscriere;
import org.example.domain.Participant;
import org.example.domain.User;
import org.example.exception.RepositoryException;
import org.example.exception.ValidationException;
import org.example.persistence.interfaces.IEventRepository;
import org.example.persistence.interfaces.IInscriereRepository;
import org.example.persistence.interfaces.IParticipantRepository;
import org.example.persistence.interfaces.IUserRepository;
import org.example.services.dto.EventDTO;
import org.example.services.dto.ParticipantEventDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of ConcursServices that delegates to individual service classes
 */
public class ConcursServicesImpl implements ConcursServices {
    private static final Logger logger = LogManager.getLogger();

    private final IUserRepository userRepository;
    private final IEventRepository eventRepository;
    private final IParticipantRepository participantRepository;
    private final IInscriereRepository inscriereRepository;

    public ConcursServicesImpl(
            IUserRepository userRepository,
            IEventRepository eventRepository,
            IParticipantRepository participantRepository,
            IInscriereRepository inscriereRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.participantRepository = participantRepository;
        this.inscriereRepository = inscriereRepository;
        logger.info("ConcursServicesImpl initialized");
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) throws ServiceException {
        try {
            logger.info("Authenticating user: {}", username);
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                logger.warn("Authentication failed: empty username or password");
                return Optional.empty();
            }
            return userRepository.authenticate(username, password);
        } catch (Exception e) {
            logger.error("Authentication error", e);
            throw new ServiceException("Authentication error: " + e.getMessage(), e);
        }
    }

    @Override
    public User registerUser(String username, String password, String officeName) throws ServiceException {
        try {
            logger.info("Registering new user: {}", username);

            // Validate input
            if (username == null || username.isEmpty()) {
                throw new ValidationException("Username cannot be empty");
            }
            if (password == null || password.isEmpty()) {
                throw new ValidationException("Password cannot be empty");
            }
            if (officeName == null || officeName.isEmpty()) {
                throw new ValidationException("Office name cannot be empty");
            }

            // Check username length
            if (username.length() < 3) {
                throw new ValidationException("Username must be at least 3 characters long");
            }

            // Check password length
            if (password.length() < 4) {
                throw new ValidationException("Password must be at least 4 characters long");
            }

            // Check if username already exists
            Optional<User> existingUser = userRepository.findByUsername(username);
            if (existingUser.isPresent()) {
                logger.warn("Username already exists: {}", username);
                throw new ValidationException("Username already exists. Please choose another username.");
            }

            // Create and save user
            User newUser = new User(username, password, officeName);
            Optional<User> savedUser = userRepository.save(newUser);

            if (savedUser.isPresent()) {
                logger.info("User registered successfully: {}", username);
                return savedUser.get();
            } else {
                logger.error("Failed to save user: {}", username);
                throw new RepositoryException("Failed to save user");
            }
        } catch (ValidationException | RepositoryException e) {
            logger.error("User registration error", e);
            throw new ServiceException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during user registration", e);
            throw new ServiceException("Unexpected error during user registration: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Event> getAllEvents() throws ServiceException {
        try {
            logger.info("Getting all events");
            return eventRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all events", e);
            throw new ServiceException("Error getting all events: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Event> getEventById(Integer id) throws ServiceException {
        try {
            logger.info("Getting event by id: {}", id);
            return eventRepository.findOne(id);
        } catch (Exception e) {
            logger.error("Error getting event by id", e);
            throw new ServiceException("Error getting event by id: " + e.getMessage(), e);
        }
    }

    @Override
    public Event createEvent(String distance, String style) throws ServiceException {
        try {
            logger.info("Creating new event: {}m {}", distance, style);
            Event event = new Event(distance, style);
            Optional<Event> savedEvent = eventRepository.save(event);

            if (savedEvent.isPresent()) {
                logger.info("Event created successfully: {}m {}", distance, style);
                return savedEvent.get();
            } else {
                logger.error("Failed to create event: {}m {}", distance, style);
                throw new RepositoryException("Failed to create event");
            }
        } catch (ValidationException | RepositoryException e) {
            logger.error("Event creation error", e);
            throw new ServiceException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during event creation", e);
            throw new ServiceException("Unexpected error during event creation: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EventDTO> getAllEventsWithParticipantCounts() throws ServiceException {
        try {
            logger.info("Getting all events with participant counts");

            List<Event> events = eventRepository.findAll();
            return events.stream()
                    .map(event -> {
                        // Get participants count for each event
                        int participantsCount = countParticipantsByEvent(event.getId());
                        return new EventDTO(
                                event.getId(),
                                event.getDistance(),
                                event.getStyle(),
                                participantsCount
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting events with participant counts", e);
            throw new ServiceException("Error getting events with participant counts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Participant> getAllParticipants() throws ServiceException {
        try {
            logger.info("Getting all participants");
            return participantRepository.findAll();
        } catch (Exception e) {
            logger.error("Error getting all participants", e);
            throw new ServiceException("Error getting all participants: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Participant> getParticipantById(Integer id) throws ServiceException {
        try {
            logger.info("Getting participant by id: {}", id);
            return participantRepository.findOne(id);
        } catch (Exception e) {
            logger.error("Error getting participant by id", e);
            throw new ServiceException("Error getting participant by id: " + e.getMessage(), e);
        }
    }

    @Override
    public Inscriere registerParticipant(String name, int age, List<Integer> eventIds) throws ServiceException {
        try {
            logger.info("Registering participant: {} for {} events", name, eventIds.size());

            // Validate input
            if (name == null || name.trim().isEmpty()) {
                throw new ValidationException("Name cannot be empty");
            }
            if (age < 5 || age > 100) {
                throw new ValidationException("Age must be between 5 and 100");
            }
            if (eventIds == null || eventIds.isEmpty()) {
                throw new ValidationException("At least one event must be selected");
            }

            // Create participant
            Participant participant = new Participant(name, age);
            Optional<Participant> savedParticipant = participantRepository.save(participant);

            if (savedParticipant.isEmpty()) {
                logger.error("Failed to save participant: {}", name);
                throw new RepositoryException("Failed to save participant");
            }

            // Get saved participant
            participant = savedParticipant.get();

            // Create registration
            Inscriere inscriere = new Inscriere(participant);

            // Add events to registration
            List<Event> events = eventIds.stream()
                    .map(eventRepository::findOne)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            if (events.size() != eventIds.size()) {
                logger.warn("Some events were not found");
            }

            inscriere.setEvents(events);

            // Save registration
            try {
                inscriereRepository.save(inscriere);
                logger.info("Participant registered successfully: {} for {} events", name, events.size());
            } catch (Exception e) {
                logger.error("Failed to register participant for events", e);
                // Try to rollback by deleting the participant
                participantRepository.delete(participant.getId());
                throw new RepositoryException("Failed to register participant for events: " + e.getMessage());
            }

            return inscriere;
        } catch (ValidationException | RepositoryException e) {
            logger.error("Participant registration error", e);
            throw new ServiceException(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during participant registration", e);
            throw new ServiceException("Unexpected error during participant registration: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Event> getEventsByParticipant(Integer participantId) throws ServiceException {
        try {
            logger.info("Getting events for participant: {}", participantId);
            Iterable<Event> events = inscriereRepository.findAllEventsByParticipant(participantId);
            return StreamSupport.stream(events.spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting events by participant", e);
            throw new ServiceException("Error getting events by participant: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Participant> getParticipantsByEvent(Integer eventId) throws ServiceException {
        try {
            logger.info("Getting participants for event: {}", eventId);
            Iterable<Participant> participants = inscriereRepository.findAllParticipantsByEvent(eventId);
            return StreamSupport.stream(participants.spliterator(), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting participants by event", e);
            throw new ServiceException("Error getting participants by event: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ParticipantEventDTO> getParticipantsWithEventCount(Integer eventId) throws ServiceException {
        try {
            logger.info("Getting participants with event count for event: {}", eventId);
            return getParticipantsByEvent(eventId).stream()
                    .map(participant -> {
                        int eventsCount = countEventsByParticipant(participant.getId());
                        return new ParticipantEventDTO(
                                participant.getId(),
                                participant.getName(),
                                participant.getAge(),
                                eventsCount
                        );
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting participants with event count", e);
            throw new ServiceException("Error getting participants with event count: " + e.getMessage(), e);
        }
    }

    /**
     * Count events a participant is registered for
     * @param participantId Participant ID
     * @return Number of events
     */
    private int countEventsByParticipant(Integer participantId) {
        Iterable<Event> events = inscriereRepository.findAllEventsByParticipant(participantId);
        return (int) StreamSupport.stream(events.spliterator(), false).count();
    }

    /**
     * Count participants registered for an event
     * @param eventId Event ID
     * @return Number of participants
     */
    private int countParticipantsByEvent(Integer eventId) {
        Iterable<Participant> participants = inscriereRepository.findAllParticipantsByEvent(eventId);
        return (int) StreamSupport.stream(participants.spliterator(), false).count();
    }
}