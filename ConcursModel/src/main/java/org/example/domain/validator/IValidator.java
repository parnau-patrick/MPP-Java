package org.example.domain.validator;

/**
 * Generic validator interface for domain entities
 * @param <T> Entity type to validate
 */
public interface IValidator<T> {

    /**
     * Validate an entity
     * @param entity Entity to validate
     * @throws org.example.exception.ValidationException If validation fails
     */
    void validate(T entity);
}