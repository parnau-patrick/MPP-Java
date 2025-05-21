package org.example.persistence.impl;

import org.example.domain.Event;
import org.example.domain.Inscriere;
import org.example.domain.Participant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.persistence.interfaces.IInscriereRepository;
import org.example.persistence.utils.JdbcUtils;
import org.example.exception.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Repository implementation for registration (Inscriere) data
 */
public class InscriereRepositoryImpl extends RepositoryBase implements IInscriereRepository {
    private static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;

    /**
     * Constructor for InscriereRepositoryImpl
     * @param props Database connection properties
     */
    public InscriereRepositoryImpl(Properties props) {
        logger.info("Initializing InscriereRepository with properties: {}", props);
        this.dbUtils = new JdbcUtils(props);
    }

    /**
     * Find all events a participant is registered for
     * @param participantId Participant ID
     * @return Iterable of events the participant is registered for
     */
    @Override
    public Iterable<Event> findAllEventsByParticipant(Integer participantId) {
        logger.traceEntry("Finding all events by participant id: {}", participantId);

        Connection connection = dbUtils.getConnection();
        List<Event> events = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT e.id AS event_id, e.distance, e.style " +
                        "FROM event_participants ep " +
                        "INNER JOIN events e ON e.id = ep.event_id " +
                        "WHERE ep.participant_id = ?")) {
            preparedStatement.setInt(1, participantId);
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Integer id = result.getInt("event_id");
                    String distance = result.getString("distance");
                    String style = result.getString("style");
                    Event event = new Event(distance, style);
                    event.setId(id);
                    events.add(event);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error fetching events for participant", e);
        }
        logger.traceExit("Found {} events for participant id: {}");
        return events;
    }

    /**
     * Find all participants registered for an event
     * @param eventId Event ID
     * @return Iterable of participants registered for the event
     */
    @Override
    public Iterable<Participant> findAllParticipantsByEvent(Integer eventId) {
        logger.traceEntry("Finding all participants by event id: {}", eventId);

        Connection connection = dbUtils.getConnection();
        List<Participant> participants = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "SELECT p.id AS participant_id, p.name, p.age " +
                        "FROM event_participants ep " +
                        "INNER JOIN participants p ON p.id = ep.participant_id " +
                        "WHERE ep.event_id = ?")) {
            preparedStatement.setInt(1, eventId);
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Integer id = result.getInt("participant_id");
                    String name = result.getString("name");
                    Integer age = result.getInt("age");
                    Participant participant = new Participant(name, age);
                    participant.setId(id);
                    participants.add(participant);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error fetching participants for event", e);
        }
        logger.traceExit("Found {} participants for event id: {}");
        return participants;
    }

    /**
     * Associate an event with a participant (register participant for event)
     * @param eventId Event ID
     * @param participantId Participant ID
     */
    @Override
    public void saveEventParticipant(Integer eventId, Integer participantId) {
        logger.traceEntry("Saving event-participant relationship: eventId={}, participantId={}", eventId, participantId);

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO event_participants (event_id, participant_id) VALUES (?, ?)")) {
            preparedStatement.setInt(1, eventId);
            preparedStatement.setInt(2, participantId);
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                logger.warn("No event-participant relationship saved");
            } else {
                // Notify observers
                notifyObservers();
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error saving event-participant relationship", e);
        }
        logger.traceExit("Event-participant relationship saved");
    }

    /**
     * Remove an event-participant association
     * @param eventId Event ID
     * @param participantId Participant ID
     */
    @Override
    public void deleteEventParticipant(Integer eventId, Integer participantId) {
        logger.traceEntry("Deleting event-participant relationship: eventId={}, participantId={}", eventId, participantId);

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "DELETE FROM event_participants WHERE event_id = ? AND participant_id = ?")) {
            preparedStatement.setInt(1, eventId);
            preparedStatement.setInt(2, participantId);
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                logger.warn("No event-participant relationship deleted");
            } else {
                // Notify observers
                notifyObservers();
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error deleting event-participant relationship", e);
        }
        logger.traceExit("Event-participant relationship deleted");
    }

    /**
     * Save a complete registration (participant with multiple events)
     * @param inscriere Registration to save
     */
    @Override
    public void save(Inscriere inscriere) {
        logger.traceEntry("Saving inscriere for participant: {}", inscriere.getParticipant());

        // First ensure the participant exists and has ID
        Participant participant = inscriere.getParticipant();
        if (participant.getId() == null) {
            throw new RepositoryException("Participant must be saved first with a valid ID");
        }

        // For each event, create an association in the junction table
        for (Event event : inscriere.getEvents()) {
            if (event.getId() == null) {
                throw new RepositoryException("Event must have a valid ID");
            }
            saveEventParticipant(event.getId(), participant.getId());
        }

        logger.traceExit("Inscriere saved successfully");
    }

    /**
     * Find all registrations
     * @return List of all registrations
     */
    @Override
    public List<Inscriere> findAll() {
        logger.traceEntry("Finding all inscrieres");

        // This requires reconstructing Inscriere objects from the relational data
        // First get all participants
        List<Inscriere> inscrieres = new ArrayList<>();
        Connection connection = dbUtils.getConnection();

        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM participants")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int participantId = rs.getInt("id");

                    // Find this participant
                    try (PreparedStatement pStmt = connection.prepareStatement(
                            "SELECT id, name, age FROM participants WHERE id = ?")) {
                        pStmt.setInt(1, participantId);
                        try (ResultSet pRs = pStmt.executeQuery()) {
                            if (pRs.next()) {
                                String name = pRs.getString("name");
                                int age = pRs.getInt("age");

                                Participant participant = new Participant(name, age);
                                participant.setId(participantId);

                                // Create inscriere
                                Inscriere inscriere = new Inscriere(participant);

                                // Find all events for this participant
                                List<Event> events = new ArrayList<>();
                                Iterable<Event> participantEvents = findAllEventsByParticipant(participantId);
                                for (Event e : participantEvents) {
                                    events.add(e);
                                }

                                inscriere.setEvents(events);
                                inscrieres.add(inscriere);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving all inscrieres", e);
            throw new RepositoryException("Error retrieving all inscrieres", e);
        }

        logger.traceExit("Found {} inscrieres", inscrieres.size());
        return inscrieres;
    }
}