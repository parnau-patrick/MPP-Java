package org.example.persistence;

import org.example.domain.Entity;
import org.example.exception.RepositoryException;
import org.example.exception.ValidationException;
import org.example.observer.Observer;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for CRUD operations
 * @param <ID> Type of entity ID
 * @param <E> Type of entity
 */
public interface Repository<ID, E extends Entity<ID>> {

    /**
     * Find entity by ID
     * @param id Entity ID
     * @return Optional containing entity if found, empty optional otherwise
     */
    Optional<E> findOne(ID id);

    /**
     * Find all entities
     * @return List of all entities
     */
    List<E> findAll();

    /**
     * Save a new entity
     * @param entity Entity to save
     * @return Optional containing saved entity
     * @throws ValidationException If entity validation fails
     * @throws RepositoryException If entity already exists or other repository error
     */
    Optional<E> save(E entity) throws ValidationException, RepositoryException;

    /**
     * Update an existing entity
     * @param entity Entity to update
     * @return Optional containing updated entity
     * @throws ValidationException If entity validation fails
     * @throws RepositoryException If entity doesn't exist or other repository error
     */
    Optional<E> update(E entity) throws ValidationException, RepositoryException;

    /**
     * Delete entity by ID
     * @param id Entity ID
     * @return Optional containing deleted entity
     * @throws RepositoryException If entity doesn't exist or other repository error
     */
    Optional<E> delete(ID id) throws RepositoryException;

    /**
     * Add observer for notifications when repository changes
     * @param observer Observer to add
     */
    void addObserver(Observer observer);

    /**
     * Remove observer
     * @param observer Observer to remove
     */
    void removeObserver(Observer observer);
}