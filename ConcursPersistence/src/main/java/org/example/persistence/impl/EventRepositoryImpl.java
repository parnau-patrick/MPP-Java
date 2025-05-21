package org.example.persistence.impl;

import org.example.domain.Event;
import org.example.persistence.interfaces.IEventRepository;
import org.example.persistence.utils.JdbcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.validator.IValidator;
import org.example.exception.RepositoryException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class EventRepositoryImpl extends RepositoryBase implements IEventRepository {
    private static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;
    private final IValidator<Event> validator;

    public EventRepositoryImpl(Properties props, IValidator<Event> validator) {
        logger.info("Initializing EventRepository with properties: {}", props);
        this.dbUtils = new JdbcUtils(props);
        this.validator = validator;
    }

    private Event getEventFromResultSet(ResultSet result) throws SQLException {
        Integer id = result.getInt("id");
        String distance = result.getString("distance");
        String style = result.getString("style");
        Event event = new Event(distance, style);
        event.setId(id);
        return event;
    }

    @Override
    public Optional<Event> findOne(Integer id) {
        logger.traceEntry("Finding event by id: {}", id);
        Connection connection = dbUtils.getConnection();

        Event event = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM events WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) {
                    event = getEventFromResultSet(result);
                    logger.trace("Found event: {}", event);
                } else {
                    logger.warn("No event found with id: {}", id);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding event by id", e);
        }
        logger.traceExit("Finding event by id: {}", id);
        return Optional.ofNullable(event);
    }

    @Override
    public List<Event> findAll() {
        logger.traceEntry("Finding all events");

        Connection connection = dbUtils.getConnection();
        List<Event> events = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM events")) {
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Event event = getEventFromResultSet(result);
                    events.add(event);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding all events", e);
        }
        logger.traceExit("Finding all events");
        return events;
    }

    @Override
    public Optional<Event> save(Event entity) {
        logger.traceEntry("Saving event: {}", entity);

        // Validate event
        validator.validate(entity);

        Connection connection = dbUtils.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO events (distance, style) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, entity.getDistance());
            preparedStatement.setString(2, entity.getStyle());
            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                logger.warn("No event saved");
            } else {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getInt(1));
                        // Notify observers
                        notifyObservers();
                        logger.traceExit("Saving event: {}", entity);
                        return Optional.of(entity);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error saving event", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Event> delete(Integer id) {
        logger.traceEntry("Deleting event with id: {}", id);

        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID must not be null or negative");
        }

        Optional<Event> existing = findOne(id);
        if (existing.isEmpty()) {
            throw new RepositoryException("No event found with id: " + id);
        }

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM events WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                logger.warn("No event deleted");
            } else {
                // Notify observers
                notifyObservers();
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error deleting event", e);
        }
        logger.traceExit("Deleting event with id: {}", id);
        return existing;
    }

    @Override
    public Optional<Event> update(Event entity) {
        logger.traceEntry("Updating event: {}", entity);

        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        // Validate event
        validator.validate(entity);

        if (findOne(entity.getId()).isEmpty()) {
            logger.warn("Event with ID {} does not exist for update", entity.getId());
            return Optional.empty();
        }

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE events SET distance = ?, style = ? WHERE id = ?")) {

            preparedStatement.setString(1, entity.getDistance());
            preparedStatement.setString(2, entity.getStyle());
            preparedStatement.setInt(3, entity.getId());
            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                logger.warn("No event updated");
            } else {
                // Notify observers
                notifyObservers();
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error updating event", e);
        }
        logger.traceExit("Updating event: {}", entity);
        return Optional.empty();
    }
}