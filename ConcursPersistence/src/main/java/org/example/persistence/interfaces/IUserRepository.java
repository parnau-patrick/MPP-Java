package org.example.persistence.interfaces;

import org.example.domain.User;
import org.example.persistence.Repository;

import java.util.Optional;

/**
 * Repository interface for User entities
 */
public interface IUserRepository extends Repository<Integer, User> {

    /**
     * Find a user by username
     * @param username Username to search for
     * @return Optional containing the user if found, empty optional otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Authenticate a user with username and password
     * @param username Username
     * @param password Password (plain text)
     * @return Optional containing the authenticated user if credentials are valid, empty optional otherwise
     */
    Optional<User> authenticate(String username, String password);
}