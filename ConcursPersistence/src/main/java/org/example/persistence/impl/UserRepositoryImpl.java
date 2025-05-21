package org.example.persistence.impl;

import org.example.domain.User;
import org.example.persistence.interfaces.IUserRepository;
import org.example.persistence.utils.JdbcUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.domain.validator.IValidator;
import org.example.exception.RepositoryException;
import org.example.persistence.utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

/**
 * Repository implementation for User entities with password encryption
 */
public class UserRepositoryImpl extends RepositoryBase implements IUserRepository {
    private static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;
    private final IValidator<User> validator;

    /**
     * Constructor for UserRepositoryImpl
     * @param props Database connection properties
     * @param validator Validator for User entities
     */
    public UserRepositoryImpl(Properties props, IValidator<User> validator) {
        logger.info("Initializing UserRepository with properties: {}", props);
        this.dbUtils = new JdbcUtils(props);
        this.validator = validator;
    }

    /**
     * Create a User object from a ResultSet
     * @param result ResultSet containing user data
     * @return User object populated with data from ResultSet
     * @throws SQLException If database access error occurs
     */
    private User getUserFromResultSet(ResultSet result) throws SQLException {
        Integer id = result.getInt("id");
        String username = result.getString("username");
        String password = result.getString("password");
        String officeName = result.getString("office_name");
        User user = new User(username, password, officeName);
        user.setId(id);
        return user;
    }

    /**
     * Find user by ID
     * @param id User ID
     * @return Optional containing user if found, empty optional otherwise
     */
    @Override
    public Optional<User> findOne(Integer id) {
        logger.traceEntry("Finding user by id: {}", id);
        Connection connection = dbUtils.getConnection();

        User user = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) {
                    user = getUserFromResultSet(result);
                    logger.trace("Found user: {}", user);
                } else {
                    logger.warn("No user found with id: {}", id);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding user by id", e);
        }
        logger.traceExit("Finding user by id: {}", id);
        return Optional.ofNullable(user);
    }

    /**
     * Find user by username
     * @param username Username to search for
     * @return Optional containing user if found, empty optional otherwise
     */
    @Override
    public Optional<User> findByUsername(String username) {
        logger.traceEntry("Finding user by username: {}", username);
        Connection connection = dbUtils.getConnection();

        User user = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet result = preparedStatement.executeQuery()) {
                if (result.next()) {
                    user = getUserFromResultSet(result);
                    logger.trace("Found user: {}", user);
                } else {
                    logger.warn("No user found with username: {}", username);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding user by username", e);
        }
        logger.traceExit("Finding user by username: {}", username);
        return Optional.ofNullable(user);
    }

    /**
     * Authenticate a user with username and password
     * @param username Username
     * @param password Plain text password
     * @return Optional containing authenticated user if credentials are valid, empty optional otherwise
     */
    @Override
    public Optional<User> authenticate(String username, String password) {
        logger.traceEntry("Authenticating user: {}", username);
        Optional<User> userOpt = findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Verify the password using jBCrypt's checkpw method
            if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                logger.info("User authenticated successfully: {}", username);
                return userOpt;
            } else {
                logger.warn("Authentication failed for user: {} - incorrect password", username);
            }
        }

        logger.traceExit("Authentication failed for user: {}", username);
        return Optional.empty();
    }

    /**
     * Find all users
     * @return List of all users
     */
    @Override
    public List<User> findAll() {
        logger.traceEntry("Finding all users");

        Connection connection = dbUtils.getConnection();
        List<User> users = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users")) {
            try (ResultSet result = preparedStatement.executeQuery()) {
                while (result.next()) {
                    User user = getUserFromResultSet(result);
                    users.add(user);
                }
            }
        } catch (Exception e) {
            logger.error(e);
            throw new RepositoryException("Error finding all users", e);
        }
        logger.traceExit("Finding all users");
        return users;
    }

    /**
     * Save a new user
     * @param entity User to save
     * @return Optional containing saved user
     * @throws RepositoryException If saving fails
     */
    @Override
    public Optional<User> save(User entity) {
        logger.traceEntry("Saving user: {}", entity);

        // Validate user
        validator.validate(entity);

        // Encrypt the password before saving
        String encryptedPassword = PasswordUtil.encryptPassword(entity.getPassword());
        entity.setPassword(encryptedPassword);

        Connection connection = dbUtils.getConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "INSERT INTO users (username, password, office_name) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getPassword()); // Now encrypted
            preparedStatement.setString(3, entity.getOfficeName());
            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                logger.warn("No user saved");
            } else {
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        entity.setId(generatedKeys.getInt(1));
                        // Notify observers
                        notifyObservers();
                        logger.traceExit("Saving user: {}", entity);
                        return Optional.of(entity);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error saving user", e);
        }
        return Optional.empty();
    }

    /**
     * Delete user by ID
     * @param id User ID
     * @return Optional containing deleted user
     * @throws RepositoryException If deletion fails
     */
    @Override
    public Optional<User> delete(Integer id) {
        logger.traceEntry("Deleting user with id: {}", id);

        if (id == null || id < 0) {
            throw new IllegalArgumentException("ID must not be null or negative");
        }

        Optional<User> existing = findOne(id);
        if (existing.isEmpty()) {
            throw new RepositoryException("No user found with id: " + id);
        }

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            preparedStatement.setInt(1, id);
            int result = preparedStatement.executeUpdate();
            if (result == 0) {
                logger.warn("No user deleted");
            } else {
                // Notify observers
                notifyObservers();
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error deleting user", e);
        }
        logger.traceExit("Deleting user with id: {}", id);
        return existing;
    }

    /**
     * Update an existing user
     * @param entity User to update
     * @return Optional containing updated user
     * @throws RepositoryException If update fails
     */
    @Override
    public Optional<User> update(User entity) {
        logger.traceEntry("Updating user: {}", entity);

        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        // Validate user
        validator.validate(entity);

        if (findOne(entity.getId()).isEmpty()) {
            logger.warn("User with ID {} does not exist for update", entity.getId());
            return Optional.empty();
        }

        // Check if password was changed - with BCrypt, hashed passwords start with $2a$
        // This approach relies on the standard BCrypt hash format to identify already hashed passwords
        if (!entity.getPassword().startsWith("$2a$")) {
            // Password appears to be plaintext, encrypt it
            String encryptedPassword = PasswordUtil.encryptPassword(entity.getPassword());
            entity.setPassword(encryptedPassword);
        }

        Connection connection = dbUtils.getConnection();
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                "UPDATE users SET username = ?, password = ?, office_name = ? WHERE id = ?")) {

            preparedStatement.setString(1, entity.getUsername());
            preparedStatement.setString(2, entity.getPassword()); // Now encrypted if it was changed
            preparedStatement.setString(3, entity.getOfficeName());
            preparedStatement.setInt(4, entity.getId());
            int result = preparedStatement.executeUpdate();

            if (result == 0) {
                logger.warn("No user updated");
            } else {
                // Notify observers
                notifyObservers();
                return Optional.of(entity);
            }
        } catch (SQLException e) {
            logger.error(e);
            throw new RepositoryException("Error updating user", e);
        }
        logger.traceExit("Updating user: {}", entity);
        return Optional.empty();
    }
}