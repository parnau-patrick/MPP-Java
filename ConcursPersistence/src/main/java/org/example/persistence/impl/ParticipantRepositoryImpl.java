package org.example.persistence.impl;

import org.example.domain.Participant;
import org.example.persistence.interfaces.IParticipantRepository;
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

public class ParticipantRepositoryImpl extends RepositoryBase implements IParticipantRepository {
    private static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;
    private final IValidator<Participant> validator;

    public ParticipantRepositoryImpl(Properties props, IValidator<Participant> validator) {
        logger.info("Initializing ParticipantRepository with properties: {}", props);
        this.dbUtils = new JdbcUtils(props);
        this.validator = validator;
    }

    private Participant getParticipantFromResultSet(ResultSet result) throws SQLException {
        Integer id = result.getInt("id");
        String name = result.getString("name");
        Integer age = result.getInt("age");
        Participant participant = new Participant(name, age);
        participant.setId(id);
        return participant;
    }

    @Override
    public Optional<Participant> findOne(Integer id) {
        logger.traceEntry("Finding participant by id: {}", id);
        Connection connection = dbUtils.getConnection();

        Participant participant = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM participants WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) {
                    participant = getParticipantFromResultSet(result);
                    logger.trace("Found participant: {}", participant);
                } else {
                    logger.warn("No participant found with id: {}", id);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding participant by id", e);
        }
        logger.traceExit("Finding participant by id: {}", id);
        return Optional.ofNullable(participant);
    }

    @Override
    public List<Participant> findAll() {
        logger.traceEntry("Finding all participants");

        Connection connection = dbUtils.getConnection();
        List<Participant> participants = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM participants")) {
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    Participant participant = getParticipantFromResultSet(result);
                    participants.add(participant);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding all participants", e);
        }
        logger.traceExit("Finding all participants");
        return participants;
    }

    @Override
    public Optional<Participant> save(Participant entity) {
        logger.traceEntry("Saving participant: {}", entity);

        // Validate participant
        validator.validate(entity);

        Connection connection = dbUtils.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO participants (name, age) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, entity.getName());
            preparedStatement.setInt(2, entity.getAge());
            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                logger.warn("No participant saved");
            } else {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getInt(1));
                        // Notify observers
                        notifyObservers();
                        logger.traceExit("Saving participant: {}", entity);
                        return Optional.of(entity);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error saving participant", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Participant> delete(Integer id) {
        logger.traceEntry("Deleting participant with id: {}", id);

        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID must not be null or negative");
        }

        Optional<Participant> existing = findOne(id);
        if (existing.isEmpty()) {
            throw new RepositoryException("No participant found with id: " + id);
        }

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM participants WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                logger.warn("No participant deleted");
            } else {
                // Notify observers
                notifyObservers();
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error deleting participant", e);
        }
        logger.traceExit("Deleting participant with id: {}", id);
        return existing;
    }

    @Override
    public Optional<Participant> update(Participant entity) {
        logger.traceEntry("Updating participant: {}", entity);

        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        // Validate participant
        validator.validate(entity);

        if (findOne(entity.getId()).isEmpty()) {
            logger.warn("Participant with ID {} does not exist for update", entity.getId());
            return Optional.empty();
        }

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE participants SET name = ?, age = ? WHERE id = ?")) {

            preparedStatement.setString(1, entity.getName());
            preparedStatement.setInt(2, entity.getAge());
            preparedStatement.setInt(3, entity.getId());
            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                logger.warn("No participant updated");
            } else {
                // Notify observers
                notifyObservers();
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error updating participant", e);
        }
        logger.traceExit("Updating participant: {}", entity);
        return Optional.empty();
    }
}